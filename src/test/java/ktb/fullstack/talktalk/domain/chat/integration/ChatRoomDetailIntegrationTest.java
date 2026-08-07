package ktb.fullstack.talktalk.domain.chat.integration;

import ktb.fullstack.talktalk.domain.chat.dto.response.ChatRoomDetailResponseDto;
import ktb.fullstack.talktalk.domain.chat.entity.ChatRoom;
import ktb.fullstack.talktalk.domain.chat.entity.ChatRoomMember;
import ktb.fullstack.talktalk.domain.chat.repository.ChatRoomMemberRepository;
import ktb.fullstack.talktalk.domain.chat.repository.ChatRoomRepository;
import ktb.fullstack.talktalk.domain.chat.repository.MessageRepository;
import ktb.fullstack.talktalk.domain.chat.service.ChatRoomQueryService;
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
public class ChatRoomDetailIntegrationTest {

    @Autowired
    UserRepository userRepository;

    @Autowired
    ChatRoomRepository chatRoomRepository;

    @Autowired
    ChatRoomMemberRepository chatRoomMemberRepository;

    @Autowired
    MessageRepository messageRepository;

    @Autowired
    ChatRoomQueryService chatRoomQueryService;

    Long meId;
    Long partnerId;
    Long emptyRoomId;

    @BeforeEach
    void setUp() {

        messageRepository.deleteAll();
        chatRoomMemberRepository.deleteAll();
        chatRoomRepository.deleteAll();
        userRepository.deleteAll();

        User me = userRepository.save(new User("me@a.a", "pw", "me"));
        User partner = userRepository.save(new User("alice@a.a", "pw", "alice"));
        ChatRoom room = saveRoom(me, partner);

        meId = me.getId();
        partnerId = partner.getId();
        emptyRoomId = room.getId();
    }

    private ChatRoom saveRoom(User a, User b) {

        ChatRoom room = chatRoomRepository.save(ChatRoom.dm(DmKey.of(a.getId(), b.getId())));
        chatRoomMemberRepository.save(new ChatRoomMember(room, a));
        chatRoomMemberRepository.save(new ChatRoomMember(room, b));
        return room;
    }

    @Test
    @DisplayName("메시지 0개인 채팅방에서도 상대방 정보를 조립해 반환한다")
    void 메시지_없는_채팅방_상대방_정보_반환() {

        ChatRoomDetailResponseDto result = chatRoomQueryService.getRoom(emptyRoomId, meId);

        assertThat(result.roomId()).isEqualTo(emptyRoomId);
        assertThat(result.partner().getId()).isEqualTo(partnerId);
        assertThat(result.partner().getNickname()).isEqualTo("alice");
    }
}
