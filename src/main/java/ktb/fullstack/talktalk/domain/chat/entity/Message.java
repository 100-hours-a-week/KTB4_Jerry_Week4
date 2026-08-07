package ktb.fullstack.talktalk.domain.chat.entity;

import jakarta.persistence.*;
import ktb.fullstack.talktalk.domain.user.entity.User;
import ktb.fullstack.talktalk.global.common.entity.BaseTimeEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Entity
@Table(name = "messages",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_message_idempotency",
                columnNames = {"room_id", "sender_id", "client_message_id"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Message extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "room_id", nullable = false)
    private ChatRoom room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "client_message_id", nullable = false, updatable = false)
    private String clientMessageId;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public Message(ChatRoom room, User sender, String content, String clientMessageId) {
        this.room = room;
        this.sender = sender;
        this.content = content;
        this.clientMessageId = clientMessageId;
    }

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }
}
