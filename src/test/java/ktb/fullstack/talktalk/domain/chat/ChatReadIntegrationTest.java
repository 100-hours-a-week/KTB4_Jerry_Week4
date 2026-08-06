package ktb.fullstack.talktalk.domain.chat;

import ktb.fullstack.talktalk.domain.chat.entity.ChatRoom;
import ktb.fullstack.talktalk.domain.chat.entity.ChatRoomMember;
import ktb.fullstack.talktalk.domain.chat.entity.Message;
import ktb.fullstack.talktalk.domain.chat.repository.ChatRoomMemberRepository;
import ktb.fullstack.talktalk.domain.chat.repository.ChatRoomRepository;
import ktb.fullstack.talktalk.domain.chat.repository.MessageRepository;
import ktb.fullstack.talktalk.domain.chat.service.ChatReadService;
import ktb.fullstack.talktalk.domain.chat.service.DmKey;
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
public class ChatReadIntegrationTest {

    @Autowired
    UserRepository userRepository;

    @Autowired
    ChatRoomRepository chatRoomRepository;

    @Autowired
    ChatRoomMemberRepository chatRoomMemberRepository;

    @Autowired
    MessageRepository messageRepository;

    @Autowired
    ChatReadService chatReadService;

    Long meId;
    Long roomId;

    @BeforeEach
    void setUp() {
        messageRepository.deleteAll();
        chatRoomMemberRepository.deleteAll();
        chatRoomRepository.deleteAll();
        userRepository.deleteAll();

        User me = userRepository.save(new User("me@a.a", "Password123!", "me"));
        User partner = userRepository.save(new User("partner@a.a", "Password123!", "partner"));

        ChatRoom room = chatRoomRepository.save(ChatRoom.dm(DmKey.of(me.getId(), partner.getId())));
        chatRoomMemberRepository.save(new ChatRoomMember(room, me));
        chatRoomMemberRepository.save(new ChatRoomMember(room, partner));

        messageRepository.save(new Message(room, me, "mine-1", "c1"));
        messageRepository.save(new Message(room, partner, "yours-1", "c2"));
        messageRepository.save(new Message(room, partner, "yours-2", "c3"));

        meId = me.getId();
        roomId = room.getId();
    }


    @Test
    @DisplayName("안 읽은 수는 내가 보낸 것과 이미 읽은 것을 제외한다")
    void 안_읽은_수_계산() {

        assertThat(chatReadService.getUnreadCount(roomId, meId)).isEqualTo(2);

        Long firstYours = messageRepository.findAll().stream()
                .filter(m -> m.getContent().equals("yours-1"))
                .findFirst().orElseThrow().getId();
        chatReadService.markRead(roomId, meId, firstYours);

        assertThat(chatReadService.getUnreadCount(roomId, meId)).isEqualTo(1);
    }


    @Test
    @DisplayName("읽음 포인터를 가장 최신 메시지까지 올리면 안 읽은 수는 0이다")
    void 모두_읽음_처리_후_0() {

        Long latest = messageRepository.findAll().stream()
                .mapToLong(Message::getId).max().orElseThrow();

        chatReadService.markRead(roomId, meId, latest);

        assertThat(chatReadService.getUnreadCount(roomId, meId)).isZero();
    }
}
