package ktb.fullstack.talktalk.domain.chat.entity;

import jakarta.persistence.*;
import ktb.fullstack.talktalk.global.common.entity.BaseTimeEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "chat_rooms",
        uniqueConstraints = @UniqueConstraint(name = "uk_chat_room_dm_key", columnNames = "dm_key")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoom extends BaseTimeEntity {

    private static final int PREVIEW_MAX_LENGTH = 50;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private RoomType type;

    @Column(name = "dm_key", length = 40)
    private String dmKey;

    @Column(name = "last_message_id")
    private Long lastMessageId;

    @Column(name = "last_message_preview", length = 100)
    private String lastMessagePreview;

    @Column(name = "last_message_at")
    private LocalDateTime lastMessageAt;

    private ChatRoom(RoomType type, String dmKey) {
        this.type = type;
        this.dmKey = dmKey;
    }

    public static ChatRoom dm(String dmKey) {
        return new ChatRoom(RoomType.DM, dmKey);
    }

    public void updateLastMessage(Message message) {

        if (message.getId() == null) return;
        if (lastMessageId != null && message.getId() <= lastMessageId) return;

        this.lastMessageId = message.getId();
        this.lastMessagePreview = toPreview(message.getContent());
        this.lastMessageAt = message.getCreatedAt();
    }

    private static String toPreview(String content) {

        if (content == null) return null;
        return content.length() <= PREVIEW_MAX_LENGTH ? content : content.substring(0, PREVIEW_MAX_LENGTH);
    }
}
