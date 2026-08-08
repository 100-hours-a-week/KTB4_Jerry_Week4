package ktb.fullstack.talktalk.domain.chat.dto.response;

import ktb.fullstack.talktalk.domain.user.dto.WriterDto;

import java.time.LocalDateTime;

public record ChatRoomEventDto(
        Long roomId,
        WriterDto partner,
        String lastMessagePreview,
        LocalDateTime lastMessageAt
) {
}
