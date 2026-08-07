package ktb.fullstack.talktalk.domain.chat.unit;

import ktb.fullstack.talktalk.domain.chat.entity.ChatRoom;
import ktb.fullstack.talktalk.domain.chat.entity.Message;
import ktb.fullstack.talktalk.domain.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class ChatRoomTest {

    private Message message(long id, String content) {

        Message m = new Message(
                ChatRoom.dm("x:y"),
                new User("a@a.a", "pw", "n"), content, "cid-" + id);
        ReflectionTestUtils.setField(m, "id", id);
        ReflectionTestUtils.setField(m, "createdAt", LocalDateTime.of(2026, 8, 6, 21, 0));
        return m;
    }

    @Test
    @DisplayName("최신 메시지의 id, 미리보기, 생성시간을 반영한다")
    void 마지막_메시지_반영() {

        ChatRoom room = ChatRoom.dm("x:y");

        room.updateLastMessage(message(10L, "Hi"));

        assertThat(room.getLastMessageId()).isEqualTo(10L);
        assertThat(room.getLastMessagePreview()).isEqualTo("Hi");
        assertThat(room.getLastMessageAt()).isEqualTo(LocalDateTime.of(2026, 8, 6, 21, 0));
    }

    @Test
    @DisplayName("더 작은 id의 메시지는 마지막 메시지를 되돌리지 않는다")
    void 작은_id_무시() {

        ChatRoom room = ChatRoom.dm("x:y");
        room.updateLastMessage(message(30L, "higher"));

        room.updateLastMessage(message(20L, "lower"));

        assertThat(room.getLastMessageId()).isEqualTo(30L);
        assertThat(room.getLastMessagePreview()).isEqualTo("higher");
    }

    @Test
    @DisplayName("긴 내용은 미리보기 길이(50자)로 잘린다")
    void 미리보기_길이_제한() {

        ChatRoom room = ChatRoom.dm("x:y");
        String longContent = "글".repeat(120);

        room.updateLastMessage(message(1L, longContent));

        assertThat(room.getLastMessagePreview()).hasSize(50);
    }
}
