package ktb.fullstack.talktalk.domain.chat.integration;

import ktb.fullstack.talktalk.domain.chat.dto.response.ChatRoomListResponseDto;
import ktb.fullstack.talktalk.domain.chat.dto.response.ChatRoomSummaryDto;
import ktb.fullstack.talktalk.domain.chat.entity.ChatRoom;
import ktb.fullstack.talktalk.domain.chat.entity.ChatRoomMember;
import ktb.fullstack.talktalk.domain.chat.repository.ChatRoomMemberRepository;
import ktb.fullstack.talktalk.domain.chat.repository.ChatRoomRepository;
import ktb.fullstack.talktalk.domain.chat.repository.MessageRepository;
import ktb.fullstack.talktalk.domain.chat.service.ChatReadService;
import ktb.fullstack.talktalk.domain.chat.service.ChatRoomListService;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class ChatRoomListIntegrationTest {

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

    @Autowired
    ChatReadService chatReadService;

    @Autowired
    ChatRoomListService chatRoomListService;

    Long meId;
    Long roomWithAliceId;
    Long roomWithBobId;

    @BeforeEach
    void setUp() {

        messageRepository.deleteAll();
        chatRoomMemberRepository.deleteAll();
        chatRoomRepository.deleteAll();
        userRepository.deleteAll();

        User me = userRepository.save(new User("me@a.a", "pw", "me"));
        User alice = userRepository.save(new User("alice@a.a", "pw", "alice"));
        User bob = userRepository.save(new User("bob@a.a", "pw", "bob"));
        User carol = userRepository.save(new User("carol@a.a", "pw", "carol"));

        ChatRoom roomAlice = saveRoom(me, alice);
        ChatRoom roomBob = saveRoom(me, bob);
        saveRoom(me, carol);

        messageService.send(roomAlice.getId(), me.getId(), "a1-mine", "c1");
        messageService.send(roomAlice.getId(), alice.getId(), "a2", "c2");
        messageService.send(roomAlice.getId(), alice.getId(), "a3-last", "c3");
        messageService.send(roomBob.getId(), bob.getId(), "b1-last", "c4");

        meId = me.getId();
        roomWithAliceId = roomAlice.getId();
        roomWithBobId = roomBob.getId();
    }

    private ChatRoom saveRoom(User a, User b) {

        ChatRoom room = chatRoomRepository.save(ChatRoom.dm(DmKey.of(a.getId(), b.getId())));
        chatRoomMemberRepository.save(new ChatRoomMember(room, a));
        chatRoomMemberRepository.save(new ChatRoomMember(room, b));
        return room;
    }

    @Test
    @DisplayName("최근 활동순으로 채팅방 목록을 반환하고, 채팅방마다 상대방, 미리보기, 안 읽은 수를 담는다")
    void 채팅방_목록_조립() {

        ChatRoomListResponseDto result = chatRoomListService.getMyRooms(meId, null);
        List<ChatRoomSummaryDto> items = result.getRooms().getItems();

        assertThat(items).hasSize(2);

        ChatRoomSummaryDto first = items.getFirst();
        assertThat(first.roomId()).isEqualTo(roomWithBobId);
        assertThat(first.partner().getNickname()).isEqualTo("bob");
        assertThat(first.lastMessagePreview()).isEqualTo("b1-last");
        assertThat(first.unreadCount()).isEqualTo(1);

        ChatRoomSummaryDto second = items.get(1);
        assertThat(second.roomId()).isEqualTo(roomWithAliceId);
        assertThat(second.partner().getNickname()).isEqualTo("alice");
        assertThat(second.lastMessagePreview()).isEqualTo("a3-last");
        assertThat(second.unreadCount()).isEqualTo(2);

        assertThat(result.getRooms().getNextCursor()).isNull();
    }

    @Test
    @DisplayName("채팅방 개수가 PAGE_SIZE를 넘으면 다음 페이지에 이어받는다")
    void 채팅방_페이지네이션() {

        User me = userRepository.findById(meId).orElseThrow();
        for (int i = 0; i < 20; i++) {
            User partner = userRepository.save(new User("p" + i + "@a.a", "pw", "p" + i));
            ChatRoom room = saveRoom(me, partner);
            messageService.send(room.getId(), partner.getId(), "m" + i, "cid-p" + i);
        }

        ChatRoomListResponseDto page1 = chatRoomListService.getMyRooms(meId, null);
        List<Long> page1Ids = page1.getRooms().getItems().stream()
                .map(ChatRoomSummaryDto::roomId).toList();
        Long cursor = page1.getRooms().getNextCursor();

        assertThat(page1Ids).hasSize(20);
        assertThat(cursor).isNotNull();

        ChatRoomListResponseDto page2 = chatRoomListService.getMyRooms(meId, cursor);
        List<Long> page2Ids = page2.getRooms().getItems().stream()
                .map(ChatRoomSummaryDto::roomId).toList();

        assertThat(page2Ids).hasSize(2);
        assertThat(page2Ids).doesNotContainAnyElementsOf(page1Ids);
        assertThat(page2.getRooms().getNextCursor()).isNull();
    }


    @Test
    @DisplayName("읽음 처리하면 채팅방 목록의 안 읽음 수가 줄어든다")
    void 읽음_반영() {

        Long lastId = messageRepository.findAll().stream()
                .filter(m -> m.getContent().equals("a3-last"))
                .findFirst().orElseThrow().getId();
        chatReadService.markRead(roomWithAliceId, meId, lastId);

        ChatRoomListResponseDto result = chatRoomListService.getMyRooms(meId, null);
        ChatRoomSummaryDto alice = result.getRooms().getItems().stream()
                .filter(r -> r.roomId().equals(roomWithAliceId))
                .findFirst().orElseThrow();

        assertThat(alice.unreadCount()).isZero();
    }
}
