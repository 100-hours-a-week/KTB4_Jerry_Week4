package ktb.fullstack.talktalk.domain.chat.service;

import ktb.fullstack.talktalk.domain.chat.dto.response.MessageResponseDto;
import ktb.fullstack.talktalk.domain.chat.entity.ChatRoom;
import ktb.fullstack.talktalk.domain.chat.entity.Message;
import ktb.fullstack.talktalk.domain.chat.repository.ChatRoomRepository;
import ktb.fullstack.talktalk.domain.chat.repository.MessageRepository;
import ktb.fullstack.talktalk.domain.user.entity.User;
import ktb.fullstack.talktalk.domain.user.repository.UserRepository;
import ktb.fullstack.talktalk.global.exception.BusinessException;
import ktb.fullstack.talktalk.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;

    @Transactional
    public MessageResponseDto send(Long roomId, Long senderId, String content) {

        if (content == null || content.isEmpty()) {
            throw new BusinessException(ErrorCode.EMPTY_MESSAGE);
        }

        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHATROOM_NOT_FOUND));
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_TOKEN));

        Message message = messageRepository.save(new Message(room, sender, content));
        return MessageResponseDto.from(message);
    }
}
