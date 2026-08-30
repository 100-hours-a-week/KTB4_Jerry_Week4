package ktb.fullstack.talktalk.domain.chat.entity;

import ktb.fullstack.talktalk.global.common.id.IdGenerator;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.UUID;

@Document(collection = "messages")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Message implements Persistable<UUID> {

    @Id
    private UUID id;

    @Field("roomId")
    private UUID roomId;

    @Field("senderId")
    private Long senderId;

    private String content;

    private String clientMessageId;

    private LocalDateTime deletedAt;

    @CreatedDate
    private LocalDateTime createdAt;

    public Message(UUID roomId, Long senderId, String content, String clientMessageId) {
        this.id = IdGenerator.nextId();
        this.roomId = roomId;
        this.senderId = senderId;
        this.content = content;
        this.clientMessageId = clientMessageId;
        this.createdAt = LocalDateTime.now();
    }

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    @Transient
    private boolean isNew = true;

    @Override
    public boolean isNew() {
        return isNew;
    }

    public void markNotNew() {
        this.isNew = false;
    }
}
