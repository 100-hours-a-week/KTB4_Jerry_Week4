package ktb.fullstack.talktalk.domain.chat.dto.request;

import jakarta.validation.constraints.NotNull;

public record ChatRoomCreateRequestDto(@NotNull Long partnerId) {
}
