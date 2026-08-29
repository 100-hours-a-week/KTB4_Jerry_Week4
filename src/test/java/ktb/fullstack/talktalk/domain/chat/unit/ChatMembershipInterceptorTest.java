package ktb.fullstack.talktalk.domain.chat.unit;

import ktb.fullstack.talktalk.domain.chat.interceptor.ChatMembershipInterceptor;
import ktb.fullstack.talktalk.domain.chat.repository.ChatRoomMemberRepository;
import ktb.fullstack.talktalk.global.exception.BusinessException;
import ktb.fullstack.talktalk.global.exception.ErrorCode;
import ktb.fullstack.talktalk.global.resolver.LoginUserInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class ChatMembershipInterceptorTest {

    @Mock
    ChatRoomMemberRepository chatRoomMemberRepository;

    @InjectMocks
    ChatMembershipInterceptor interceptor;

    private static final UUID ROOM_ID = UUID.fromString("0198f3a2-7c40-7000-8a3f-1c2d3e4f5060");

    private Authentication user(Long userId) {

        return new UsernamePasswordAuthenticationToken(
                new LoginUserInfo(userId, 1L), null, List.of());
    }

    private Message<byte[]> frame(StompCommand command, String destination, Authentication user) {

        StompHeaderAccessor accessor = StompHeaderAccessor.create(command);
        accessor.setLeaveMutable(true);
        accessor.setDestination(destination);
        accessor.setUser(user);
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }

    @Test
    @DisplayName("채팅방 멤버의 구독은 허용된다")
    void 멤버_구독_허용() {

        given(chatRoomMemberRepository.existsByRoomIdAndUserId(ROOM_ID, 5L)).willReturn(true);
        Message<byte[]> message = frame(StompCommand.SUBSCRIBE, "/topic/chat/rooms/0198f3a2-7c40-7000-8a3f-1c2d3e4f5060", user(5L));

        assertThatCode(() -> interceptor.preSend(message, null)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("채팅방 멤버의 전송은 허용된다")
    void 멤버_전송_허용() {

        given(chatRoomMemberRepository.existsByRoomIdAndUserId(ROOM_ID, 5L)).willReturn(true);
        Message<byte[]> message = frame(StompCommand.SEND, "/app/chat/rooms/0198f3a2-7c40-7000-8a3f-1c2d3e4f5060", user(5L));

        assertThatCode(() -> interceptor.preSend(message, null)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("채팅방 비멤버의 구독은 거부된다")
    void 비멤버_구독_거부() {

        given(chatRoomMemberRepository.existsByRoomIdAndUserId(ROOM_ID, 5L)).willReturn(false);
        Message<byte[]> message = frame(StompCommand.SUBSCRIBE, "/topic/chat/rooms/0198f3a2-7c40-7000-8a3f-1c2d3e4f5060", user(5L));

        assertThatThrownBy(() -> interceptor.preSend(message, null))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.NOT_CHATROOM_MEMBER);
    }

    @Test
    @DisplayName("채팅방 비멤버의 전송은 거부된다")
    void 비멤버_전송_거부() {

        given(chatRoomMemberRepository.existsByRoomIdAndUserId(ROOM_ID, 5L)).willReturn(false);
        Message<byte[]> message = frame(StompCommand.SEND, "/app/chat/rooms/0198f3a2-7c40-7000-8a3f-1c2d3e4f5060", user(5L));

        assertThatThrownBy(() -> interceptor.preSend(message, null))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.NOT_CHATROOM_MEMBER);
    }

    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {"/user/queue/acks", "/user/queue/errors", "/user/queue/rooms"})
    @DisplayName("개인 큐 구독은 멤버십 검사 없이 허용된다")
    void 개인_큐_구독_허용(String destination) {

        Message<byte[]> message = frame(StompCommand.SUBSCRIBE, destination, user(5L));

        assertThatCode(() -> interceptor.preSend(message, null)).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/topic/**",
            "/topic/other",
            "/topic/chat/rooms/0198f3a2-7c40-7000-8a3f-1c2d3e4f5060/extra",
            "/topic/evil/chat/rooms/0198f3a2-7c40-7000-8a3f-1c2d3e4f5060",
            "/topic/chat/rooms/abc",
            "/queue/**",
            "/queue/acks-user",
            "/user/queue/**",
            "/app/chat/rooms/0198f3a2-7c40-7000-8a3f-1c2d3e4f5060"
    })
    @DisplayName("구독 대상 목적지 형식이 아닌 구독은 거부된다")
    void 알_수_없는_목적지_구독_거부(String destination) {

        Message<byte[]> message = frame(StompCommand.SUBSCRIBE, destination, user(5L));

        assertThatThrownBy(() -> interceptor.preSend(message, null))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.INVALID_DESTINATION);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/topic/chat/rooms/0198f3a2-7c40-7000-8a3f-1c2d3e4f5060",
            "/app/chat/rooms/0198f3a2-7c40-7000-8a3f-1c2d3e4f5060/extra",
            "/app/chat/rooms",
            "/app/other",
            "/user/queue/acks"
    })
    @DisplayName("메시지 전송 대상 목적지 형식이 아닌 전송은 거부된다")
    void 알_수_없는_목적지_전송_거부(String destination) {

        Message<byte[]> message = frame(StompCommand.SEND, destination, user(5L));

        assertThatThrownBy(() -> interceptor.preSend(message, null))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.INVALID_DESTINATION);
    }

    @Test
    @DisplayName("SUBSCRIBE, SEND가 아닌 프레임은 검사하지 않는다")
    void 비대상_커맨드_통과() {

        Message<byte[]> message = frame(StompCommand.UNSUBSCRIBE, "/topic/**", user(5L));

        assertThatCode(() -> interceptor.preSend(message, null)).doesNotThrowAnyException();
    }
}
