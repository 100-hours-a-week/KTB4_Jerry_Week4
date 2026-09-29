package ktb.fullstack.talktalk.domain.chat.service;

import ktb.fullstack.talktalk.domain.chat.entity.LastMessage;
import ktb.fullstack.talktalk.domain.chat.entity.Message;
import ktb.fullstack.talktalk.domain.chat.repository.ChatRoomRepository;
import ktb.fullstack.talktalk.domain.chat.repository.ChatRoomUpdater;
import ktb.fullstack.talktalk.domain.chat.repository.MessageRepository;
import ktb.fullstack.talktalk.domain.user.repository.UserRepository;
import ktb.fullstack.talktalk.global.exception.BusinessException;
import ktb.fullstack.talktalk.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class MessageWriter {

    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;
    private final ChatRoomUpdater chatRoomUpdater;

    public Message write(UUID roomId, Long senderId, String content, String clientMessageId) {

        if (!chatRoomRepository.existsById(roomId)) {
            throw new BusinessException(ErrorCode.CHATROOM_NOT_FOUND);
        }

        userRepository.findById(senderId)
                .filter(user -> user.getDeletedAt() == null)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_TOKEN));

        Message message = messageRepository.save(new Message(roomId, senderId, content, clientMessageId));
        chatRoomUpdater.applyLastMessage(roomId, LastMessage.from(message));
        return message;
    }
}
