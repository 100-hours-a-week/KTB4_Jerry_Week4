package ktb.fullstack.talktalk.domain.chat.integration;

import ktb.fullstack.talktalk.domain.chat.entity.ChatRoom;
import ktb.fullstack.talktalk.domain.chat.entity.ChatRoomMember;
import ktb.fullstack.talktalk.domain.chat.entity.LastMessage;
import ktb.fullstack.talktalk.domain.chat.entity.Message;
import ktb.fullstack.talktalk.domain.chat.repository.ChatRoomRepository;
import ktb.fullstack.talktalk.domain.chat.repository.ChatRoomUpdater;
import ktb.fullstack.talktalk.support.MongoTestContainerConfig;
import ktb.fullstack.talktalk.support.MySqlTestContainerConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("mongotest")
@Import({ MySqlTestContainerConfig.class, MongoTestContainerConfig.class })
class ChatRoomUpdaterIntegrationTest {

    @Autowired ChatRoomRepository chatRoomRepository;
    @Autowired ChatRoomUpdater chatRoomUpdater;

    UUID roomId;

    private static UUID msgId(long n) {
        return UUID.fromString(String.format("0198f3a2-7c40-7000-b000-%012d", n));
    }

    private static Message messageOf(long n, String content) {
        Message message = new Message(UUID.randomUUID(), 1L, content, "cid-" + n);
        ReflectionTestUtils.setField(message, "id", msgId(n));
        return message;
    }

    @BeforeEach
    void setUp() {
        chatRoomRepository.deleteAll();
        ChatRoom room = ChatRoom.dm("1:2");
        room.addMember(1L);
        room.addMember(2L);
        roomId = chatRoomRepository.save(room).getId();
    }

    @Test
    @DisplayName("마지막 메시지는 더 큰 id로만 전진한다")
    void 마지막_메시지_전진_규칙() {

        chatRoomUpdater.applyLastMessage(roomId, LastMessage.from(messageOf(30, "higher")));
        chatRoomUpdater.applyLastMessage(roomId, LastMessage.from(messageOf(20, "lower")));

        ChatRoom room = chatRoomRepository.findById(roomId).orElseThrow();
        assertThat(room.getLastMessageId()).isEqualTo(msgId(30));
        assertThat(room.getLastMessagePreview()).isEqualTo("higher");
    }

    @Test
    @DisplayName("미리보기는 50자로 잘린다")
    void 미리보기_길이_제한() {

        chatRoomUpdater.applyLastMessage(roomId, LastMessage.from(messageOf(10, "글".repeat(120))));

        ChatRoom room = chatRoomRepository.findById(roomId).orElseThrow();
        assertThat(room.getLastMessagePreview()).hasSize(50);
    }

    @Test
    @DisplayName("마지막 메시지는 지정한 값으로 교체되고, 값이 없으면 제거된다")
    void 마지막_메시지_재설정() {

        chatRoomUpdater.applyLastMessage(roomId, LastMessage.from(messageOf(10, "Hi")));
        chatRoomUpdater.applyLastMessage(roomId, LastMessage.from(messageOf(20, "Bye")));

        chatRoomUpdater.resetLastMessage(roomId, LastMessage.from(messageOf(10, "Hi")));

        ChatRoom replaced = chatRoomRepository.findById(roomId).orElseThrow();
        assertThat(replaced.getLastMessageId()).isEqualTo(msgId(10));
        assertThat(replaced.getLastMessagePreview()).isEqualTo("Hi");

        chatRoomUpdater.resetLastMessage(roomId, null);

        ChatRoom cleared = chatRoomRepository.findById(roomId).orElseThrow();
        assertThat(cleared.getLastMessageId()).isNull();
        assertThat(cleared.getLastMessagePreview()).isNull();
    }

    @Test
    @DisplayName("읽음 포인터는 더 큰 값으로만 전진한다")
    void 읽음_포인터_전진_규칙() {

        chatRoomUpdater.advanceLastRead(roomId, 1L, msgId(30));
        chatRoomUpdater.advanceLastRead(roomId, 1L, msgId(20));

        ChatRoom room = chatRoomRepository.findById(roomId).orElseThrow();
        assertThat(room.memberOf(1L).map(ChatRoomMember::getLastReadMessageId)).contains(msgId(30));
        assertThat(room.memberOf(2L).map(ChatRoomMember::getLastReadMessageId)).isEmpty();
    }
}
