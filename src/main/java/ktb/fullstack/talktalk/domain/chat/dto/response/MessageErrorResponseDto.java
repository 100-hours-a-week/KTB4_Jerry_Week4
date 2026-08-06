package ktb.fullstack.talktalk.domain.chat.dto.response;

import ktb.fullstack.talktalk.global.exception.ErrorCode;

public record MessageErrorResponseDto(
        String code,
        String message,
        String clientMessageId
) {

    public static MessageErrorResponseDto of(ErrorCode errorCode, String clientMessageId) {

        return new MessageErrorResponseDto(errorCode.name(), errorCode.getMessage(), clientMessageId);
    }
}
