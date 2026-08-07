package ktb.fullstack.talktalk.domain.chat.service;

import ktb.fullstack.talktalk.domain.chat.dto.response.MessageListResponseDto;
import ktb.fullstack.talktalk.domain.chat.dto.response.MessageResponseDto;
import ktb.fullstack.talktalk.domain.chat.entity.ChatRoom;
import ktb.fullstack.talktalk.domain.chat.entity.Message;
import ktb.fullstack.talktalk.domain.chat.repository.ChatRoomMemberRepository;
import ktb.fullstack.talktalk.domain.chat.repository.MessageRepository;
import ktb.fullstack.talktalk.global.common.response.CursorPageResponse;
import ktb.fullstack.talktalk.global.exception.BusinessException;
import ktb.fullstack.talktalk.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService {

    private static final int PAGE_SIZE = 30;

    private final MessageRepository messageRepository;
    private final MessageWriter messageWriter;
    private final ChatRoomMemberRepository chatRoomMemberRepository;

    @Transactional(readOnly = true)
    public MessageListResponseDto getMessages(Long roomId, Long requesterId, Long cursor) {

        if (!chatRoomMemberRepository.existsByRoomIdAndUserId(roomId, requesterId)) {
            throw new BusinessException(ErrorCode.NOT_CHATROOM_MEMBER);
        }

        List<Message> messages = messageRepository.
                findByRoomIdAndCursor(roomId, cursor, PageRequest.of(0, PAGE_SIZE + 1));

        boolean hasNext = messages.size() > PAGE_SIZE;
        List<Message> pageContent = hasNext ? messages.subList(0, PAGE_SIZE) : messages;
        Long nextCursor = hasNext ? messages.getLast().getId() : null;

        List<MessageResponseDto> items = pageContent.stream().map(MessageResponseDto::from).toList();
        return new MessageListResponseDto(new CursorPageResponse<>(items, nextCursor));
    }

    public MessageResponseDto send(Long roomId, Long senderId, String content, String clientMessageId) {

        if (content == null || content.isEmpty()) {
            throw new BusinessException(ErrorCode.EMPTY_MESSAGE);
        }

        if (clientMessageId == null || clientMessageId.isBlank()) {
            throw new BusinessException(ErrorCode.EMPTY_CLIENT_MESSAGE_ID);
        }

        Message message = messageRepository
                .findByRoomIdAndSenderIdAndClientMessageId(roomId, senderId, clientMessageId)
                .orElseGet(() -> saveOrRecover(roomId, senderId, content, clientMessageId));

        return MessageResponseDto.from(message);
    }

    @Transactional
    public void deleteMessage(Long roomId, Long messageId, Long requesterId) {

        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MESSAGE_NOT_FOUND));

        if (!message.getRoom().getId().equals(roomId)) {
            throw new BusinessException(ErrorCode.MESSAGE_NOT_FOUND);
        }
        if (!message.getSender().getId().equals(requesterId)) {
            throw new BusinessException(ErrorCode.NOT_MESSAGE_OWNER);
        }
        if (message.isDeleted()) return;

        message.softDelete();

        ChatRoom room = message.getRoom();
        if (messageId.equals(room.getLastMessageId())) {
            Message latest = messageRepository.findTopByRoomIdAndDeletedAtIsNullOrderByIdDesc(roomId).orElse(null);
            room.resetLastMessage(latest);
        }
    }

    private Message saveOrRecover(Long roomId, Long senderId, String content, String clientMessageId) {

        try {
            return messageWriter.write(roomId, senderId, content, clientMessageId);

        } catch (DataIntegrityViolationException race) {
            return messageRepository
                    .findByRoomIdAndSenderIdAndClientMessageId(roomId, senderId, clientMessageId)
                    .orElseThrow(() -> race);
        }
    }
}
