package ktb.fullstack.talktalk.domain.chat.entity;

import jakarta.persistence.*;
import ktb.fullstack.talktalk.global.common.entity.BaseTimeEntity;
import ktb.fullstack.talktalk.global.common.id.IdGenerator;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Persistable;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "chat_rooms",
        uniqueConstraints = @UniqueConstraint(name = "uk_chat_room_dm_key", columnNames = "dm_key")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoom extends BaseTimeEntity implements Persistable<UUID> {

    private static final int PREVIEW_MAX_LENGTH = 50;

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private RoomType type;

    @Column(name = "dm_key", length = 40)
    private String dmKey;

    @Column(name = "last_message_id")
    private UUID lastMessageId;

    @Column(name = "last_message_preview", length = 100)
    private String lastMessagePreview;

    @Column(name = "last_message_at")
    private LocalDateTime lastMessageAt;

    private ChatRoom(RoomType type, String dmKey) {
        this.id = IdGenerator.nextId();
        this.type = type;
        this.dmKey = dmKey;
    }

    public static ChatRoom dm(String dmKey) {
        return new ChatRoom(RoomType.DM, dmKey);
    }

    public void updateLastMessage(Message message) {

        if (message.getId() == null) return;
        if (lastMessageId != null && message.getId().compareTo(lastMessageId) <= 0) return;

        this.lastMessageId = message.getId();
        this.lastMessagePreview = toPreview(message.getContent());
        this.lastMessageAt = message.getCreatedAt();
    }

    public void resetLastMessage(Message message) {

        if (message == null) {
            this.lastMessageId = null;
            this.lastMessagePreview = null;
            this.lastMessageAt = null;
            return;
        }
        this.lastMessageId = message.getId();
        this.lastMessagePreview = toPreview(message.getContent());
        this.lastMessageAt = message.getCreatedAt();
    }

    private static String toPreview(String content) {

        if (content == null) return null;
        return content.length() <= PREVIEW_MAX_LENGTH ? content : content.substring(0, PREVIEW_MAX_LENGTH);
    }

    @Transient
    private boolean isNew = true;

    @Override
    public boolean isNew() {
        return isNew;
    }

    @PostPersist
    @PostLoad
    void markNotNew() {
        this.isNew = false;
    }
}
