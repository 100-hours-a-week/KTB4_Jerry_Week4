package ktb.fullstack.talktalk.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    INVALID_EMAIL(HttpStatus.BAD_REQUEST, "invalid_email"),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "invalid_password"),
    INVALID_NICKNAME(HttpStatus.BAD_REQUEST, "invalid_nickname"),

    INVALID_TITLE(HttpStatus.BAD_REQUEST, "invalid_title"),
    INVALID_CONTENT(HttpStatus.BAD_REQUEST, "invalid_content"),

    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "invalid_credentials"),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "invalid_token"),

    NOT_POST_OWNER(HttpStatus.FORBIDDEN, "not_post_owner"),
    NOT_COMMENT_OWNER(HttpStatus.FORBIDDEN, "not_comment_owner"),

    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "post_not_found"),
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "comment_not_found"),

    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "email_already_exists"),
    NICKNAME_ALREADY_EXISTS(HttpStatus.CONFLICT, "nickname_already_exists"),

    TOO_LARGE_FILE(HttpStatus.CONTENT_TOO_LARGE, "too_large_file"),

    UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "unsupported_media_type"),


    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "internal_server_error");

    private final HttpStatus status;

    private final String message;
}
