package ktb.fullstack.talktalk.domain.chat.unit;

import ktb.fullstack.talktalk.domain.chat.dto.response.MessageListResponseDto;
import ktb.fullstack.talktalk.domain.chat.dto.response.MessageResponseDto;
import ktb.fullstack.talktalk.domain.chat.entity.ChatRoom;
import ktb.fullstack.talktalk.domain.chat.entity.Message;
import ktb.fullstack.talktalk.domain.chat.repository.ChatRoomMemberRepository;
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
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
public class MessageServiceTest {

    @Mock
    MessageRepository messageRepository;

    @Mock
    MessageWriter messageWriter;

    @Mock
    ChatRoomMemberRepository chatRoomMemberRepository;

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

    @Nested
    @DisplayName("메시지 이력 조회")
    class History {

        private List<Message> messagesDesc(int count) {

            List<Message> list = new ArrayList<>();
            for (int i = count; i >= 1; i--) {
                list.add(messageFixture((long) i, 1L, 5L, "m" + i, "cid-" + i));
            }
            return list;
        }

        @Test
        @DisplayName("채팅방 멤버가 아니면 NOT_CHATROOM_MEMBER 예외")
        void 비멤버_거부() {

            given(chatRoomMemberRepository.existsByRoomIdAndUserId(1L, 5L)).willReturn(false);

            assertThatThrownBy(() -> messageService.getMessages(1L, 5L, null))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode").isEqualTo(ErrorCode.NOT_CHATROOM_MEMBER);

            then(messageRepository).should(never()).findByRoomIdAndCursor(any(), any(), any());
        }

        @Test
        @DisplayName("다음 페이지가 있으면 PAGE_SIZE개만 반환하고 nextCursor로 다음 시작점을 준다")
        void 다음_페이지_있음() {

            given(chatRoomMemberRepository.existsByRoomIdAndUserId(1L, 5L)).willReturn(true);
            given(messageRepository.findByRoomIdAndCursor(eq(1L), isNull(), any(Pageable.class)))
                    .willReturn(messagesDesc(31));

            MessageListResponseDto result = messageService.getMessages(1L, 5L, null);

            assertThat(result.getMessages().getItems()).hasSize(30);
            assertThat(result.getMessages().getNextCursor()).isEqualTo(1L);
        }

        @Test
        @DisplayName("마지막 페이지면 남은 메시지를 전부 반환하고 nextCursor는 null이다")
        void 마지막_페이지() {

            given(chatRoomMemberRepository.existsByRoomIdAndUserId(1L, 5L)).willReturn(true);
            given(messageRepository.findByRoomIdAndCursor(eq(1L), isNull(), any(Pageable.class)))
                    .willReturn(messagesDesc(5));

            MessageListResponseDto result = messageService.getMessages(1L, 5L, null);

            assertThat(result.getMessages().getItems()).hasSize(5);
            assertThat(result.getMessages().getNextCursor()).isNull();
        }
    }

    @Nested
    @DisplayName("삭제")
    class Delete {

        @Test
        @DisplayName("메시지가 없으면 MESSAGE_NOT_FOUND 예외")
        void 메시지_없음() {

            given(messageRepository.findById(10L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> messageService.deleteMessage(1L, 10L ,5L))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode").isEqualTo(ErrorCode.MESSAGE_NOT_FOUND);
        }


        @Test
        @DisplayName("다른 방의 메시지면 MESSAGE_NOT_FOUND 예외")
        void 다른_채팅방_메시지() {

            given(messageRepository.findById(10L))
                    .willReturn(Optional.of(messageFixture(10L, 1L, 5L, "Hi", "c1")));

            assertThatThrownBy(() -> messageService.deleteMessage(2L, 10L ,5L))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode").isEqualTo(ErrorCode.MESSAGE_NOT_FOUND);
        }

        @Test
        @DisplayName("발신자가 아니면 NOT_MESSAGE_OWNER 예외")
        void 발신자_아님() {

            given(messageRepository.findById(10L))
                    .willReturn(Optional.of(messageFixture(10L, 1L, 5L, "Hi", "c1")));

            assertThatThrownBy(() -> messageService.deleteMessage(1L, 10L, 99L))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode").isEqualTo(ErrorCode.NOT_MESSAGE_OWNER);
        }

        @Test
        @DisplayName("이미 삭제된 메시지면 아무 것도 하지 않는다(멱등)")
        void 이미_삭제된_메시지_멱등() {

            Message deleted = messageFixture(10L, 1L, 5L, "Hi", "c1");
            ReflectionTestUtils.setField(deleted, "deletedAt", LocalDateTime.now());
            given(messageRepository.findById(10L)).willReturn(Optional.of(deleted));

            messageService.deleteMessage(1L, 10L, 5L);

            then(messageRepository).should(never()).findTopByRoomIdAndDeletedAtIsNullOrderByIdDesc(any());
        }
    }






























}