package ktb.fullstack.talktalk.domain.chat;

import ktb.fullstack.talktalk.domain.chat.dto.response.MessageResponseDto;
import ktb.fullstack.talktalk.domain.chat.entity.ChatRoom;
import ktb.fullstack.talktalk.domain.chat.entity.Message;
import ktb.fullstack.talktalk.domain.chat.repository.MessageRepository;
import ktb.fullstack.talktalk.domain.chat.service.MessageService;
import ktb.fullstack.talktalk.domain.chat.service.MessageWriter;
import ktb.fullstack.talktalk.domain.user.entity.User;
import ktb.fullstack.talktalk.global.exception.BusinessException;
import ktb.fullstack.talktalk.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
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
    MessageWriter messageWriter;

    @InjectMocks
    MessageService messageService;

    private static final String CLIENT_MESSAGE_ID = "11111111-1111-1111-1111-111111111111";


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

    private Message messageFixture(Long id, Long roomId, Long senderId, String content, String clientMessageId) {

        Message message = new Message(roomFixture(roomId), userFixture(senderId), content, clientMessageId);
        ReflectionTestUtils.setField(message, "id", id);
        ReflectionTestUtils.setField(message, "createdAt", LocalDateTime.now());
        return message;
    }

    @Nested
    @DisplayName("정상 전송")
    class Send {

        @Test
        @DisplayName("새 clientMessageId면 저장 후 응답을 반환한다")
        void 신규_전송() {

            given(messageRepository.findByRoomIdAndSenderIdAndClientMessageId(1L, 5L, CLIENT_MESSAGE_ID)).willReturn(Optional.empty());
            given(messageWriter.write(1L, 5L, "Hi\nJerry", CLIENT_MESSAGE_ID)).willReturn(messageFixture(10L, 1L, 5L, "Hi\nJerry", CLIENT_MESSAGE_ID));

            MessageResponseDto result =
                    messageService.send(1L, 5L, "Hi\nJerry", CLIENT_MESSAGE_ID);

            assertThat(result.messageId()).isEqualTo(10L);
            assertThat(result.roomId()).isEqualTo(1L);
            assertThat(result.senderId()).isEqualTo(5L);
            assertThat(result.content()).isEqualTo("Hi\nJerry");
            assertThat(result.clientMessageId()).isEqualTo(CLIENT_MESSAGE_ID);
        }
    }

    @Nested
    @DisplayName("멱등성")
    class Idempotency {

        @Test
        @DisplayName("이미 저장된 clientMessageId면 다시 저장하지 않고 기존 메시지를 반환한다")
        void 재전송_기존_메시지_반환() {

            given(messageRepository.findByRoomIdAndSenderIdAndClientMessageId(1L, 5L, CLIENT_MESSAGE_ID))
                    .willReturn(Optional.of(messageFixture(10L, 1L, 5L, "Hi", CLIENT_MESSAGE_ID)));

            MessageResponseDto result =
                    messageService.send(1L, 5L, "Hi", CLIENT_MESSAGE_ID);

            assertThat(result.messageId()).isEqualTo(10L);
            then(messageWriter).should(never()).write(any(), any(), any(), any());
        }

        @Test
        @DisplayName("동시 전송으로 UNIQUE 위반이 나면 다시 조회해 기존 메시지를 반환한다")
        void 경합_복구() {

            given(messageRepository.findByRoomIdAndSenderIdAndClientMessageId(1L, 5L, CLIENT_MESSAGE_ID))
                    .willReturn(Optional.empty())
                    .willReturn(Optional.of(messageFixture(10L, 1L, 5L, "Hi", CLIENT_MESSAGE_ID)));
            given(messageWriter.write(1L, 5L, "Hi", CLIENT_MESSAGE_ID)).willThrow(new DataIntegrityViolationException("unique violation"));

            MessageResponseDto result =
                    messageService.send(1L, 5L, "Hi", CLIENT_MESSAGE_ID);

            assertThat(result.messageId()).isEqualTo(10L);
        }
    }

    @Nested
    @DisplayName("유효성 검증")
    class Validation {

        @Test
        @DisplayName("빈 메시지는 거부한다")
        void 빈_메시지() {

            assertThatThrownBy(() -> messageService.send(1L, 5L, "", CLIENT_MESSAGE_ID))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode").isEqualTo(ErrorCode.EMPTY_MESSAGE);

            then(messageWriter).should(never()).write(any(),any(), any(), any());
        }

        @Test
        @DisplayName("clientMessageId가 없으면 거부한다")
        void 빈_클라이언트ID() {

            assertThatThrownBy(() -> messageService.send(1L, 5L, "Hi\nJerry", ""))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode").isEqualTo(ErrorCode.EMPTY_CLIENT_MESSAGE_ID);

            then(messageWriter).should(never()).write(any(), any(), any(), any());
        }
    }
}