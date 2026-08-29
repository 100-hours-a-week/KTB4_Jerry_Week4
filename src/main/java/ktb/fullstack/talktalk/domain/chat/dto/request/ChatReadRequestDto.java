package ktb.fullstack.talktalk.domain.chat.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ChatReadRequestDto(@NotNull UUID lastReadMessageId) {
}
