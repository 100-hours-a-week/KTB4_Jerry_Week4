package ktb.fullstack.talktalk.domain.chat.integration;

import ktb.fullstack.talktalk.domain.chat.dto.response.MessageListResponseDto;
import ktb.fullstack.talktalk.domain.chat.dto.response.MessageResponseDto;
import ktb.fullstack.talktalk.domain.chat.entity.ChatRoom;
import ktb.fullstack.talktalk.domain.chat.entity.Message;
import ktb.fullstack.talktalk.domain.chat.repository.ChatRoomRepository;
import ktb.fullstack.talktalk.domain.chat.repository.MessageRepository;
import ktb.fullstack.talktalk.domain.chat.service.DmKey;
import ktb.fullstack.talktalk.domain.chat.service.MessageService;
import ktb.fullstack.talktalk.domain.user.entity.User;
import ktb.fullstack.talktalk.domain.user.repository.UserRepository;
import ktb.fullstack.talktalk.global.exception.BusinessException;
import ktb.fullstack.talktalk.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import ktb.fullstack.talktalk.support.MySqlTestContainerConfig;
import ktb.fullstack.talktalk.support.MongoTestContainerConfig;
import org.springframework.context.annotation.Import;

@SpringBootTest
@ActiveProfiles("mongotest")
@Import({ MySqlTestContainerConfig.class, MongoTestContainerConfig.class })
public class MessageHistoryIntegrationTest {

    @Autowired
    UserRepository userRepository;

    @Autowired
    ChatRoomRepository chatRoomRepository;

    @Autowired
    MessageRepository messageRepository;

    @Autowired
    MessageService messageService;

    Long memberId;
    Long outsiderId;
    UUID roomId;

    @BeforeEach
    void setUp() {

        messageRepository.deleteAll();
        chatRoomRepository.deleteAll();
        userRepository.deleteAll();

        User member = userRepository.save(new User("member@a.a", "Password123!", "member"));
        User partner = userRepository.save(new User("partner@a.a", "Password123!", "partner"));
        User outsider = userRepository.save(new User("outsider@a.a", "Password123!", "outsider"));

        ChatRoom room = ChatRoom.dm(DmKey.of(member.getId(), partner.getId()));
        room.addMember(member.getId());
        room.addMember(partner.getId());
        chatRoomRepository.save(room);

        ChatRoom otherRoom = chatRoomRepository.save(ChatRoom.dm("99:100"));

        messageRepository.save(new Message(room.getId(), member.getId(), "first", "cid-1"));
        messageRepository.save(new Message(room.getId(), member.getId(), "second", "cid-2"));
        messageRepository.save(new Message(room.getId(), member.getId(), "third", "cid-3"));
        messageRepository.save(new Message(otherRoom.getId(), member.getId(), "other-room", "cid-x"));

        memberId = member.getId();
        outsiderId = outsider.getId();
        roomId = room.getId();
    }

    @Test
    @DisplayName("해당 채팅방 메시지만 최신순으로 반환한다")
    void 채팅방_메시지_최신순_반환() {

        MessageListResponseDto result = messageService.getMessages(roomId, memberId, null);

        assertThat(result.getMessages().getItems()).hasSize(3);
        assertThat(result.getMessages().getItems()).extracting(MessageResponseDto::content)
                .containsExactly("third", "second", "first");
        assertThat(result.getMessages().getNextCursor()).isNull();
    }

    @Test
    @DisplayName("cursor 이하의 메시지만 반환한다")
    void 커서_이하_메시지만_반환() {

        UUID secondId = messageRepository.findAll().stream()
                .filter(m -> m.getContent().equals("second"))
                .findFirst().orElseThrow().getId();

        MessageListResponseDto result = messageService.getMessages(roomId, memberId, secondId);

        assertThat(result.getMessages().getItems()).extracting(MessageResponseDto::content)
                .containsExactly("second", "first");

    }

    @Test
    @DisplayName("채팅방 멤버가 아니면 메시지 조회를 거부한다")
    void 비멤버_조회_거부() {

        assertThatThrownBy(() -> messageService.getMessages(roomId, outsiderId, null))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.NOT_CHATROOM_MEMBER);
    }
}
