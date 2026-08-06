package ktb.fullstack.talktalk.domain.chat;

import ktb.fullstack.talktalk.domain.chat.entity.ChatRoom;
import ktb.fullstack.talktalk.domain.chat.entity.Message;
import ktb.fullstack.talktalk.domain.chat.repository.ChatRoomRepository;
import ktb.fullstack.talktalk.domain.chat.repository.MessageRepository;
import ktb.fullstack.talktalk.domain.chat.service.MessageWriter;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class MessageWriterTest {

    @Mock
    MessageRepository messageRepository;

    @Mock
    ChatRoomRepository chatRoomRepository;

    @Mock
    UserRepository userRepository;

    @InjectMocks
    MessageWriter messageWriter;

    private static final String CLIENT_MESSAGE_ID = "11111111-1111-1111-1111-111111111111";

    private ChatRoom roomFixture(Long id) {

        ChatRoom room = ChatRoom.dm("1:2");
        ReflectionTestUtils.setField(room, "id", id);
        return room;
    }

    private User userFixture(Long id) {

        User user = new User("e" + id + "@aaa.aaa", "pw", "n" + id);
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    @Test
    @DisplayName("채팅방과 발신자를 찾아 메시지를 저장한다")
    void 메시지_정상_저장() {

        given(chatRoomRepository.findById(1L)).willReturn(Optional.of(roomFixture(1L)));
        given(userRepository.findById(5L)).willReturn(Optional.of(userFixture(5L)));
        given(messageRepository.save(any(Message.class))).willAnswer(inv -> {
            Message m = inv.getArgument(0);
            ReflectionTestUtils.setField(m, "id", 10L);
            return m;
        });

        Message saved = messageWriter.write(1L, 5L, "Hi", CLIENT_MESSAGE_ID);

        assertThat(saved.getId()).isEqualTo(10L);
        assertThat(saved.getClientMessageId()).isEqualTo(CLIENT_MESSAGE_ID);
    }

    @Test
    @DisplayName("채팅방이 존재하지 않으면 CHATROOM_NOT_FOUND 예외")
    void 채팅방_없음() {

        given(chatRoomRepository.findById(1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> messageWriter.write(1L, 5L, "Hi", CLIENT_MESSAGE_ID))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.CHATROOM_NOT_FOUND);
    }

    @Test
    @DisplayName("발신자가 존재하지 않으면 INVALID_TOKEN 예외")
    void 발신자_없음() {

        given(chatRoomRepository.findById(1L)).willReturn(Optional.of(roomFixture(1L)));
        given(userRepository.findById(5L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> messageWriter.write(1L, 5L, "Hi", CLIENT_MESSAGE_ID))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.INVALID_TOKEN);
    }
}
