package ktb.fullstack.talktalk.domain.chat.entity;

import ktb.fullstack.talktalk.global.common.id.IdGenerator;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Document(collection = "chatRooms")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoom implements Persistable<UUID> {

    @Id
    private UUID id;

    private RoomType type;

    private String dmKey;

    private List<ChatRoomMember> members = new ArrayList<>();

    private LastMessage lastMessage;

    private LocalDateTime createdAt;

    private ChatRoom(RoomType type, String dmKey) {
        this.id = IdGenerator.nextId();
        this.type = type;
        this.dmKey = dmKey;
        this.createdAt = LocalDateTime.now();
    }

    public static ChatRoom dm(String dmKey) {
        return new ChatRoom(RoomType.DM, dmKey);
    }

    public void addMember(Long userId) {
        this.members.add(new ChatRoomMember(userId));
    }

    public Optional<ChatRoomMember> memberOf(Long userId) {
        return members.stream().filter(member -> member.getUserId().equals(userId)).findFirst();
    }

    public Optional<Long> partnerOf(Long userId) {
        return members.stream().map(ChatRoomMember::getUserId)
                .filter(id -> !id.equals(userId)).findFirst();
    }

    public UUID getLastMessageId() {
        return lastMessage == null ? null : lastMessage.getId();
    }

    public String getLastMessagePreview() {
        return lastMessage == null ? null : lastMessage.getPreview();
    }

    public LocalDateTime getLastMessageAt() {
        return lastMessage == null ? null : lastMessage.getAt();
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
