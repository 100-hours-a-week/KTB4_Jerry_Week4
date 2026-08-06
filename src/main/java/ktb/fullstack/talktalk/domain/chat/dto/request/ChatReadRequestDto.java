package ktb.fullstack.talktalk.domain.chat.dto.request;

import jakarta.validation.constraints.NotNull;

public record ChatReadRequestDto(@NotNull Long lastReadMessageId) {
}
