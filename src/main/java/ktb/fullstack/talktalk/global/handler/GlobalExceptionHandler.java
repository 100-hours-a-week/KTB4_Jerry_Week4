package ktb.fullstack.talktalk.global.handler;

import ktb.fullstack.talktalk.global.common.response.ApiResponse;
import ktb.fullstack.talktalk.global.exception.BusinessException;
import ktb.fullstack.talktalk.global.exception.ErrorCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {

        ErrorCode errorCode = e.getErrorCode();
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponse.of(errorCode.getMessage(), null));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException e) {

        String field = e.getBindingResult().getFieldErrors().getFirst().getField();
        ErrorCode errorCode = resolveInvalidFieldErrorCode(field);
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponse.of(errorCode.getMessage(), null));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleMaxUploadSize(MaxUploadSizeExceededException e) {

        ErrorCode errorCode = ErrorCode.TOO_LARGE_FILE;
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponse.of(errorCode.getMessage(), null));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {

        ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponse.of(errorCode.getMessage(), null));
    }

    private ErrorCode resolveInvalidFieldErrorCode(String field) {

        return switch (field) {
            case "email" -> ErrorCode.INVALID_EMAIL;
            case "password" -> ErrorCode.INVALID_PASSWORD;
            case "nickname" -> ErrorCode.INVALID_NICKNAME;
            case "title" -> ErrorCode.INVALID_TITLE;
            case "content" -> ErrorCode.INVALID_CONTENT;
            default -> ErrorCode.INTERNAL_SERVER_ERROR;
        };
    }
}
