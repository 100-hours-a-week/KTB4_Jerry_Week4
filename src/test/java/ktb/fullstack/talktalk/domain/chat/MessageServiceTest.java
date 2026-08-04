package ktb.fullstack.talktalk.domain.chat;

import ktb.fullstack.talktalk.domain.chat.dto.response.MessageResponseDto;
import ktb.fullstack.talktalk.domain.chat.entity.ChatRoom;
import ktb.fullstack.talktalk.domain.chat.entity.Message;
import ktb.fullstack.talktalk.domain.chat.repository.ChatRoomRepository;
import ktb.fullstack.talktalk.domain.chat.repository.MessageRepository;
import ktb.fullstack.talktalk.domain.chat.service.MessageService;
import ktb.fullstack.talktalk.domain.user.entity.User;
import ktb.fullstack.talktalk.domain.user.repository.UserRepository;
import ktb.fullstack.talktalk.global.exception.BusinessException;
import ktb.fullstack.talktalk.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
public class MessageServiceTest {

    @Mock
    MessageRepository messageRepository;

    @Mock
    ChatRoomRepository chatRoomRepository;

    @Mock
    UserRepository userRepository;

    @InjectMocks
    MessageService messageService;

    private ChatRoom roomFixture(Long id) {
        ChatRoom room = ChatRoom.dm("1:2");
        ReflectionTestUtils.setField(room, "id", id);
        return room;
    }

    private User userFixture(Long id) {
        User user = new User("e" + id + "@a.a", "pw", "n" + id);
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    @Test
    @DisplayName("정상 메시지를 저장하고 응답을 반환한다")
    void 정상_전송() {

        given(chatRoomRepository.findById(1L)).willReturn(Optional.of(roomFixture(1L)));
        given(userRepository.findById(5L)).willReturn(Optional.of(userFixture(5L)));
        given(messageRepository.save(any(Message.class))).willAnswer(inv -> {
            Message m = inv.getArgument(0);
            ReflectionTestUtils.setField(m, "id", 10L);
            ReflectionTestUtils.setField(m, "createdAt", LocalDateTime.now());
            return m;
        });

        MessageResponseDto result = messageService.send(1L, 5L, "Hi\nJerry");

        assertThat(result.messageId()).isEqualTo(10L);
        assertThat(result.roomId()).isEqualTo(1L);
        assertThat(result.senderId()).isEqualTo(5L);
        assertThat(result.content()).isEqualTo("Hi\nJerry");
    }

    @Test
    @DisplayName("빈 메시지는 거부한다")
    void 빈_메시지() {

        assertThatThrownBy(() -> messageService.send(1L, 5L, ""))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.EMPTY_MESSAGE);

        then(messageRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("채팅방 존재하지 않으면 CHATROOM_NOT_FOUND 예외")
    void 채팅방_없음() {

        given(chatRoomRepository.findById(1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> messageService.send(1L, 5L, "Hi\nJerry"))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.CHATROOM_NOT_FOUND);
    }
}