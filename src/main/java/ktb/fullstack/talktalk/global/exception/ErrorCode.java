package ktb.fullstack.talktalk.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    BAD_REQUEST(HttpStatus.BAD_REQUEST, "bad_request"),
    CANNOT_CHAT_ALONE(HttpStatus.BAD_REQUEST, "cannot_chat_alone"),
    EMPTY_MESSAGE(HttpStatus.BAD_REQUEST, "empty_message"),
    EMPTY_CLIENT_MESSAGE_ID(HttpStatus.BAD_REQUEST, "empty_client_message_id"),

    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "invalid_credentials"),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "invalid_token"),

    NOT_POST_OWNER(HttpStatus.FORBIDDEN, "not_post_owner"),
    NOT_COMMENT_OWNER(HttpStatus.FORBIDDEN, "not_comment_owner"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "forbidden"),
    NOT_CHATROOM_MEMBER(HttpStatus.FORBIDDEN, "not_chatroom_member"),

    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "post_not_found"),
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "comment_not_found"),
    IMAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "image_not_found"),
    PARTNER_NOT_FOUND(HttpStatus.NOT_FOUND, "partner_not_found"),
    CHATROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "chatroom_not_found"),

    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "email_already_exists"),
    NICKNAME_ALREADY_EXISTS(HttpStatus.CONFLICT, "nickname_already_exists"),

    CANNOT_REPLY_TO_REPLY(HttpStatus.CONFLICT, "cannot_reply_to_reply"),
    ALREADY_REPORTED(HttpStatus.CONFLICT, "already_reported"),

    TOO_LARGE_FILE(HttpStatus.CONTENT_TOO_LARGE, "too_large_file"),

    UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "unsupported_media_type"),

    TOO_MANY_POSTS_IN_SHORT(HttpStatus.TOO_MANY_REQUESTS, "too_many_posts_in_short"),

    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "internal_server_error");

    private final HttpStatus status;

    private final String message;
}
