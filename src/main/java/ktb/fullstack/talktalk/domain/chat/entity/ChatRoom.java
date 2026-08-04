package ktb.fullstack.talktalk.domain.chat.entity;

import jakarta.persistence.*;
import ktb.fullstack.talktalk.global.common.entity.BaseTimeEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "chat_rooms",
        uniqueConstraints = @UniqueConstraint(name = "uk_chat_room_dm_key", columnNames = "dm_key")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoom extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private RoomType type;

    @Column(name = "dm_key", length = 40)
    private String dmKey;

    private ChatRoom(RoomType type, String dmKey) {
        this.type = type;
        this.dmKey = dmKey;
    }

    public static ChatRoom dm(String dmKey) {
        return new ChatRoom(RoomType.DM, dmKey);
    }
}
