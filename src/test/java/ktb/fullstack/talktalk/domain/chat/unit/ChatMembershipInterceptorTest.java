package ktb.fullstack.talktalk.domain.chat.unit;

import ktb.fullstack.talktalk.domain.chat.interceptor.ChatMembershipInterceptor;
import ktb.fullstack.talktalk.domain.chat.repository.ChatRoomMemberRepository;
import ktb.fullstack.talktalk.global.exception.BusinessException;
import ktb.fullstack.talktalk.global.exception.ErrorCode;
import ktb.fullstack.talktalk.global.resolver.LoginUserInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class ChatMembershipInterceptorTest {

    @Mock
    ChatRoomMemberRepository chatRoomMemberRepository;

    @InjectMocks
    ChatMembershipInterceptor interceptor;

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

        given(chatRoomMemberRepository.existsByRoomIdAndUserId(1L, 5L)).willReturn(true);
        Message<byte[]> message = frame(StompCommand.SUBSCRIBE, "/topic/chat/rooms/1", user(5L));

        assertThatCode(() -> interceptor.preSend(message, null)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("채팅방 비멤버의 구독은 거부된다")
    void 비멤버_구독_거부() {

        given(chatRoomMemberRepository.existsByRoomIdAndUserId(1L, 5L)).willReturn(false);
        Message<byte[]> message = frame(StompCommand.SUBSCRIBE, "/topic/chat/rooms/1", user(5L));

        assertThatThrownBy(() -> interceptor.preSend(message, null))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.NOT_CHATROOM_MEMBER);
    }

    @Test
    @DisplayName("채팅방 비멤버의 전송은 거부된다")
    void 비멤버_전송_거부() {

        given(chatRoomMemberRepository.existsByRoomIdAndUserId(1L, 5L)).willReturn(false);
        Message<byte[]> message = frame(StompCommand.SEND, "/app/chat/rooms/1", user(5L));

        assertThatThrownBy(() -> interceptor.preSend(message, null))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.NOT_CHATROOM_MEMBER);
    }

    @Test
    @DisplayName("채팅방 목적지가 아니면 멤버십을 검사하지 않는다")
    void 채팅방_목적지_아니면_멤버십_검사_통과() {

        Message<byte[]> message = frame(StompCommand.SUBSCRIBE, "/topic/other", user(5L));

        assertThatCode(() -> interceptor.preSend(message, null)).doesNotThrowAnyException();
    }
}
