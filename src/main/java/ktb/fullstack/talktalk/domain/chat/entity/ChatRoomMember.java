package ktb.fullstack.talktalk.domain.chat.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoomMember {

    private Long userId;

    private UUID lastReadMessageId;

    public ChatRoomMember(Long userId) {
        this.userId = userId;
    }
}
