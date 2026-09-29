package ktb.fullstack.talktalk.domain.chat.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LastMessage {

    private static final int PREVIEW_MAX_LENGTH = 50;

    private UUID id;

    private String preview;

    private LocalDateTime at;

    private LastMessage(UUID id, String preview, LocalDateTime at) {
        this.id = id;
        this.preview = preview;
        this.at = at;
    }

    public static LastMessage from(Message message) {

        return new LastMessage(message.getId(), toPreview(message.getContent()), message.getCreatedAt());
    }

    private static String toPreview(String content) {

        if (content == null) return null;
        return content.length() <= PREVIEW_MAX_LENGTH ? content : content.substring(0, PREVIEW_MAX_LENGTH);
    }
}
