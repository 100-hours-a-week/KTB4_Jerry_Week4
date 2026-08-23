package ktb.fullstack.talktalk.domain.chat.dto.response;

import java.time.LocalDateTime;

public record MessageDeleteResult(
        MessageResponseDto message,
        boolean lastMessageChanged,
        String lastMessagePreview,
        LocalDateTime lastMessageAt
) {

    public static MessageDeleteResult lastMessageKept(MessageResponseDto message) {

        return new MessageDeleteResult(message, false, null, null);
    }

    public static MessageDeleteResult lastMessageChanged(MessageResponseDto message, String preview, LocalDateTime lastMessageAt) {

        return new MessageDeleteResult(message, true, preview, lastMessageAt);
    }
}
