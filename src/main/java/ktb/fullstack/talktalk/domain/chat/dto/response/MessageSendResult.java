package ktb.fullstack.talktalk.domain.chat.dto.response;

import ktb.fullstack.talktalk.domain.user.dto.WriterDto;

public record MessageSendResult(
        MessageResponseDto message,
        WriterDto sender
) {
}
