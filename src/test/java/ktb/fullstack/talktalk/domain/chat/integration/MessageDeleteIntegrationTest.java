package ktb.fullstack.talktalk.domain.chat.integration;

import ktb.fullstack.talktalk.domain.chat.dto.response.MessageResponseDto;
import ktb.fullstack.talktalk.domain.chat.entity.ChatRoom;
import ktb.fullstack.talktalk.domain.chat.entity.ChatRoomMember;
import ktb.fullstack.talktalk.domain.chat.repository.ChatRoomMemberRepository;
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
public class MessageDeleteIntegrationTest {

    @Autowired
    UserRepository userRepository;

    @Autowired
    ChatRoomRepository chatRoomRepository;

    @Autowired
    ChatRoomMemberRepository chatRoomMemberRepository;

    @Autowired
    MessageRepository messageRepository;

    @Autowired
    MessageService messageService;

    Long meId;
    Long partnerId;
    Long roomId;

    @BeforeEach
    void setUp() {

        messageRepository.deleteAll();
        chatRoomMemberRepository.deleteAll();
        chatRoomRepository.deleteAll();
        userRepository.deleteAll();

        User me = userRepository.save(new User("me@a.a", "pw", "me"));
        User partner = userRepository.save(new User("partner@a.a", "pw", "partner"));
        ChatRoom room = chatRoomRepository.save(ChatRoom.dm(DmKey.of(me.getId(), partner.getId())));
        chatRoomMemberRepository.save(new ChatRoomMember(room, me));
        chatRoomMemberRepository.save(new ChatRoomMember(room, partner));

        meId = me.getId();
        partnerId = partner.getId();
        roomId = room.getId();
    }

    private Long send(Long senderId, String content, String cid) {

        return messageService.send(roomId, senderId, content, cid).messageId();
    }

    @Test
    @DisplayName("삭제하면 soft delete로 처리된다")
    void 소프트삭제() {

        Long id = send(meId, "삭제하기", "c1");

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

        Long first = send(meId, "first", "c1");
        Long last = send(meId, "last", "c2");

        messageService.deleteMessage(roomId, last, meId);

        ChatRoom room = chatRoomRepository.findById(roomId).orElseThrow();
        assertThat(room.getLastMessageId()).isEqualTo(first);
        assertThat(room.getLastMessagePreview()).isEqualTo("first");
    }

    @Test
    @DisplayName("마지막이 아닌 메시지를 삭제하면 채팅방의 마지막 메시지는 그대로이다")
    void 중간_삭제하면_미리보기_유지() {

        Long first = send(meId, "first", "c1");
        Long last = send(meId, "last", "c2");

        messageService.deleteMessage(roomId, first, meId);

        ChatRoom room = chatRoomRepository.findById(roomId).orElseThrow();
        assertThat(room.getLastMessageId()).isEqualTo(last);
        assertThat(room.getLastMessagePreview()).isEqualTo("last");

    }
}
