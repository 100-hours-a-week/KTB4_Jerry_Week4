package ktb.fullstack.talktalk.domain.chat.dto.response;

import ktb.fullstack.talktalk.domain.user.dto.WriterDto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ChatRoomSummaryDto(
        UUID roomId,
        WriterDto partner,
        String lastMessagePreview,
        LocalDateTime lastMessageAt,
        long unreadCount
) {
}
