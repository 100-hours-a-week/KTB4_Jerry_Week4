package ktb.fullstack.talktalk.domain.chat.dto.response;

import ktb.fullstack.talktalk.domain.chat.entity.Message;

import java.time.LocalDateTime;
import java.util.UUID;

public record MessageResponseDto(
        UUID messageId,
        UUID roomId,
        Long senderId,
        String content,
        String clientMessageId,
        boolean deleted,
        LocalDateTime createdAt
) {
    public static MessageResponseDto from(Message message) {

        return new MessageResponseDto(
                message.getId(),
                message.getRoomId(),
                message.getSenderId(),
                message.isDeleted() ? null : message.getContent(),
                message.getClientMessageId(),
                message.isDeleted(),
                message.getCreatedAt()
        );
    }
}
