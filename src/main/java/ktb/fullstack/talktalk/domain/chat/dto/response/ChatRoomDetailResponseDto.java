package ktb.fullstack.talktalk.domain.chat.dto.response;

import ktb.fullstack.talktalk.domain.user.dto.WriterDto;

import java.util.UUID;

public record ChatRoomDetailResponseDto(
        UUID roomId,
        WriterDto partner
) {
}
