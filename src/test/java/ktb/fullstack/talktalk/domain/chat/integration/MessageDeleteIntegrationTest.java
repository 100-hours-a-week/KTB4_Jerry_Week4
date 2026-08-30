package ktb.fullstack.talktalk.domain.chat.integration;

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

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import ktb.fullstack.talktalk.support.MySqlTestContainerConfig;
import ktb.fullstack.talktalk.support.MongoTestContainerConfig;
import org.springframework.context.annotation.Import;

@SpringBootTest
@ActiveProfiles("mongotest")
@Import({ MySqlTestContainerConfig.class, MongoTestContainerConfig.class })
public class MessageDeleteIntegrationTest {

    @Autowired
    UserRepository userRepository;

    @Autowired
    ChatRoomRepository chatRoomRepository;

    @Autowired
    MessageRepository messageRepository;

    @Autowired
    MessageService messageService;

    Long meId;
    Long partnerId;
    UUID roomId;

    @BeforeEach
    void setUp() {

        messageRepository.deleteAll();
        chatRoomRepository.deleteAll();
        userRepository.deleteAll();

        User me = userRepository.save(new User("me@a.a", "pw", "me"));
        User partner = userRepository.save(new User("partner@a.a", "pw", "partner"));
        ChatRoom room = ChatRoom.dm(DmKey.of(me.getId(), partner.getId()));
        room.addMember(me.getId());
        room.addMember(partner.getId());
        chatRoomRepository.save(room);

        meId = me.getId();
        partnerId = partner.getId();
        roomId = room.getId();
    }

    private UUID send(Long senderId, String content, String cid) {

        return messageService.send(roomId, senderId, content, cid).message().messageId();
    }

    @Test
    @DisplayName("삭제하면 soft delete로 처리된다")
    void 소프트삭제() {

        UUID id = send(meId, "삭제하기", "c1");

        messageService.deleteMessage(roomId, id, meId);

        assertThat(messageRepository.findById(id).orElseThrow().getDeletedAt()).isNotNull();

        MessageResponseDto item = messageService.getMessages(roomId, meId, null).getMessages().getItems().stream()
                .filter(m -> m.messageId().equals(id)).findFirst().orElseThrow();
        assertThat(item.deleted()).isTrue();
        assertThat(item.content()).isNull();
    }

    @Test
    @DisplayName("마지막 메시지를 삭제하면 채팅방의 마지막 메시지가 직전 메시지로 재계산된다")
    void 마지막_삭제하면_미리보기_재계산() {

        UUID first = send(meId, "first", "c1");
        UUID last = send(meId, "last", "c2");

        messageService.deleteMessage(roomId, last, meId);

        ChatRoom room = chatRoomRepository.findById(roomId).orElseThrow();
        assertThat(room.getLastMessageId()).isEqualTo(first);
        assertThat(room.getLastMessagePreview()).isEqualTo("first");
    }

    @Test
    @DisplayName("마지막이 아닌 메시지를 삭제하면 채팅방의 마지막 메시지는 그대로이다")
    void 중간_삭제하면_미리보기_유지() {

        UUID first = send(meId, "first", "c1");
        UUID last = send(meId, "last", "c2");

        messageService.deleteMessage(roomId, first, meId);

        ChatRoom room = chatRoomRepository.findById(roomId).orElseThrow();
        assertThat(room.getLastMessageId()).isEqualTo(last);
        assertThat(room.getLastMessagePreview()).isEqualTo("last");

    }
}
