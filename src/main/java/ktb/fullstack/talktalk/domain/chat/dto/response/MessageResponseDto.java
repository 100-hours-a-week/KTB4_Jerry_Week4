package ktb.fullstack.talktalk.domain.chat.dto.response;

import ktb.fullstack.talktalk.domain.chat.entity.Message;

import java.time.LocalDateTime;

public record MessageResponseDto(
        Long messageId,
        Long roomId,
        Long senderId,
        String content,
        String clientMessageId,
        LocalDateTime createdAt
) {
    public static MessageResponseDto from(Message message) {

        return new MessageResponseDto(
                message.getId(),
                message.getRoom().getId(),
                message.getSender().getId(),
                message.getContent(),
                message.getClientMessageId(),
                message.getCreatedAt()
        );
    }
}
