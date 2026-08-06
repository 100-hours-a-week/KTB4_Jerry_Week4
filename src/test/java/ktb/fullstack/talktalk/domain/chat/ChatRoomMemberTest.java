package ktb.fullstack.talktalk.domain.chat;

import ktb.fullstack.talktalk.domain.chat.entity.ChatRoom;
import ktb.fullstack.talktalk.domain.chat.entity.ChatRoomMember;
import ktb.fullstack.talktalk.domain.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

public class ChatRoomMemberTest {

    private ChatRoomMember member() {

        return new ChatRoomMember(ChatRoom.dm("x:y"), new User("a@a.a", "pw", "n"));
    }

    @DisplayName("읽음 포인터는 더 큰 값으로만 전진한다")
    @ParameterizedTest(name = "현재 {0}, 들어온 {1} -> 결과 {2}")
    @CsvSource(nullValues = "null", value = {
            "null, 10,   10",
            "10,   20,   20",
            "30,   20,   30",
            "10,   null, 10"
    })
    void 읽음_포인터_전진_규칙(
            Long current,
            Long incoming,
            Long expected
    ) {

        ChatRoomMember m = member();
        if (current != null) {
            m.updateLastRead(current);
        }

        m.updateLastRead(incoming);
        assertThat(m.getLastReadMessageId()).isEqualTo(expected);
    }
}
