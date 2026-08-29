package ktb.fullstack.talktalk.domain.chat.unit;

import ktb.fullstack.talktalk.domain.chat.entity.ChatRoom;
import ktb.fullstack.talktalk.domain.chat.entity.ChatRoomMember;
import ktb.fullstack.talktalk.domain.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class ChatRoomMemberTest {

    private ChatRoomMember member() {

        return new ChatRoomMember(ChatRoom.dm("x:y"), new User("a@a.a", "pw", "n"));
    }

    @DisplayName("읽음 포인터는 더 큰 값으로만 전진한다")
    @ParameterizedTest(name = "현재 {0}, 들어온 {1} -> 결과 {2}")
    @CsvSource(nullValues = "null", value = {
            "null, 0198f3a2-7c40-7000-8a3f-000000000020, 0198f3a2-7c40-7000-8a3f-000000000020",
            "0198f3a2-7c40-7000-8a3f-000000000010, 0198f3a2-7c40-7000-8a3f-000000000020, 0198f3a2-7c40-7000-8a3f-000000000020",
            "0198f3a2-7c40-7000-8a3f-000000000030, 0198f3a2-7c40-7000-8a3f-000000000020, 0198f3a2-7c40-7000-8a3f-000000000030",
            "0198f3a2-7c40-7000-8a3f-000000000010, null, 0198f3a2-7c40-7000-8a3f-000000000010"
    })
    void 읽음_포인터_전진_규칙(
            UUID current,
            UUID incoming,
            UUID expected
    ) {

        ChatRoomMember m = member();
        if (current != null) {
            m.updateLastRead(current);
        }

        m.updateLastRead(incoming);
        assertThat(m.getLastReadMessageId()).isEqualTo(expected);
    }
}
