package ktb.fullstack.talktalk.domain.chat.service;

import ktb.fullstack.talktalk.domain.chat.dto.response.*;
import ktb.fullstack.talktalk.domain.chat.entity.ChatRoom;
import ktb.fullstack.talktalk.domain.chat.entity.Message;
import ktb.fullstack.talktalk.domain.chat.repository.ChatRoomMemberRepository;
import ktb.fullstack.talktalk.domain.chat.repository.ChatRoomRepository;
import ktb.fullstack.talktalk.domain.chat.repository.MessageRepository;
import ktb.fullstack.talktalk.domain.user.service.WriterResolver;
import ktb.fullstack.talktalk.global.exception.BusinessException;
import ktb.fullstack.talktalk.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MessageService {

    private static final int PAGE_SIZE = 30;
    private static final int MAX_CONTENT_LENGTH = 2000;

    private final MessageRepository messageRepository;
    private final MessageWriter messageWriter;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final WriterResolver writerResolver;

    @Transactional(readOnly = true)
    public MessageListResponseDto getMessages(UUID roomId, Long requesterId, UUID cursor) {

        if (!chatRoomMemberRepository.existsByRoomIdAndUserId(roomId, requesterId)) {
            throw new BusinessException(ErrorCode.NOT_CHATROOM_MEMBER);
        }

        PageRequest page = PageRequest.of(0, PAGE_SIZE + 1);
        List<Message> messages = cursor == null
                ? messageRepository.findByRoomIdOrderByIdDesc(roomId, page)
                : messageRepository.findByRoomIdAndIdLessThanEqualOrderByIdDesc(roomId, cursor, page);

        boolean hasNext = messages.size() > PAGE_SIZE;
        List<Message> pageContent = hasNext ? messages.subList(0, PAGE_SIZE) : messages;
        UUID nextCursor = hasNext ? messages.getLast().getId() : null;

        List<MessageResponseDto> items = pageContent.stream().map(MessageResponseDto::from).toList();
        return new MessageListResponseDto(new ChatCursorPageResponse<>(items, nextCursor));
    }

    public MessageSendResult send(UUID roomId, Long senderId, String content, String clientMessageId) {

        if (content == null || content.isBlank()) {
            throw new BusinessException(ErrorCode.EMPTY_MESSAGE);
        }

        if (content.length() > MAX_CONTENT_LENGTH) {
            throw new BusinessException(ErrorCode.TOO_LONG_MESSAGE);
        }

        if (clientMessageId == null || clientMessageId.isBlank()) {
            throw new BusinessException(ErrorCode.EMPTY_CLIENT_MESSAGE_ID);
        }

        Message message = messageRepository
                .findByRoomIdAndSenderIdAndClientMessageId(roomId, senderId, clientMessageId)
                .orElseGet(() -> saveOrRecover(roomId, senderId, content, clientMessageId));

        return new MessageSendResult(
                MessageResponseDto.from(message),
                writerResolver.resolveWriter(message.getSenderId()));
    }

    @Transactional
    public MessageDeleteResult deleteMessage(UUID roomId, UUID messageId, Long requesterId) {

        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MESSAGE_NOT_FOUND));

        if (!message.getRoomId().equals(roomId)) {
            throw new BusinessException(ErrorCode.MESSAGE_NOT_FOUND);
        }
        if (!message.getSenderId().equals(requesterId)) {
            throw new BusinessException(ErrorCode.NOT_MESSAGE_OWNER);
        }
        if (message.isDeleted()) {
            return MessageDeleteResult.lastMessageKept(MessageResponseDto.from(message));
        }

        message.softDelete();
        messageRepository.save(message);

        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHATROOM_NOT_FOUND));
        boolean isLastMessage = messageId.equals(room.getLastMessageId());

        if (isLastMessage) {
            Message latest = messageRepository.findTopByRoomIdAndDeletedAtIsNullOrderByIdDesc(roomId).orElse(null);
            room.resetLastMessage(latest);
        }

        MessageResponseDto deleted = MessageResponseDto.from(message);

        return isLastMessage
                ? MessageDeleteResult.lastMessageChanged(deleted, room.getLastMessagePreview(), room.getLastMessageAt())
                : MessageDeleteResult.lastMessageKept(deleted);
    }

    private Message saveOrRecover(UUID roomId, Long senderId, String content, String clientMessageId) {

        try {
            return messageWriter.write(roomId, senderId, content, clientMessageId);

        } catch (DataIntegrityViolationException race) {
            return messageRepository
                    .findByRoomIdAndSenderIdAndClientMessageId(roomId, senderId, clientMessageId)
                    .orElseThrow(() -> race);
        }
    }
}
