package ktb.fullstack.talktalk.domain.chat;

import ktb.fullstack.talktalk.domain.chat.dto.response.MessageResponseDto;
import ktb.fullstack.talktalk.domain.chat.entity.ChatRoom;
import ktb.fullstack.talktalk.domain.chat.repository.ChatRoomRepository;
import ktb.fullstack.talktalk.domain.chat.repository.MessageRepository;
import ktb.fullstack.talktalk.domain.chat.service.DmKey;
import ktb.fullstack.talktalk.domain.chat.service.MessageService;
import ktb.fullstack.talktalk.domain.user.entity.User;
import ktb.fullstack.talktalk.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class ChatRoomLastMessageIntegrationTest {

    @Autowired
    UserRepository userRepository;

    @Autowired
    ChatRoomRepository chatRoomRepository;

    @Autowired
    MessageRepository messageRepository;

    @Autowired
    MessageService messageService;

    Long meId;
    Long roomId;

    @BeforeEach
    void setUp() {
        messageRepository.deleteAll();
        chatRoomRepository.deleteAll();
        userRepository.deleteAll();

        User me = userRepository.save(new User("me@a.a", "Password123!", "me"));
        User partner = userRepository.save(new User("partner@a.a", "Password123!", "partner"));

        ChatRoom room = chatRoomRepository.save(ChatRoom.dm(DmKey.of(me.getId(), partner.getId())));

        meId = me.getId();
        roomId = room.getId();
    }

    @Test
    @DisplayName("메시지를 전송하면 채팅방의 마지막 메시지가 가장 최신 메시지로 갱신된다")
    void 전송하면_마지막_메시지_갱신() {

        messageService.send(roomId, meId, "first", "c1");
        MessageResponseDto second = messageService.send(roomId, meId, "Second Message", "c2");

        ChatRoom room = chatRoomRepository.findById(roomId).orElseThrow();
        assertThat(room.getLastMessageId()).isEqualTo(second.messageId());
        assertThat(room.getLastMessagePreview()).isEqualTo("Second Message");
        assertThat(room.getLastMessageAt()).isNotNull();
    }

    @Test
    @DisplayName("같은 clientMessageId 재전송은 마지막 메시지를 중복 갱신하지 않는다")
    void 재전송_마지막_메시지_멱등() {

        MessageResponseDto first = messageService.send(roomId, meId, "same", "c1");
        messageService.send(roomId, meId, "same", "c1");

        ChatRoom room = chatRoomRepository.findById(roomId).orElseThrow();
        assertThat(room.getLastMessageId()).isEqualTo(first.messageId());
        assertThat(room.getLastMessagePreview()).isEqualTo("same");
        assertThat(messageRepository.count()).isEqualTo(1);
    }






}
