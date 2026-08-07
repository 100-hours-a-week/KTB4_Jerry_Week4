package ktb.fullstack.talktalk.domain.chat;

import ktb.fullstack.talktalk.domain.auth.repository.SessionRepository;
import ktb.fullstack.talktalk.global.exception.BusinessException;
import ktb.fullstack.talktalk.global.exception.ErrorCode;
import ktb.fullstack.talktalk.global.interceptor.StompAuthChannelInterceptor;
import ktb.fullstack.talktalk.global.jwt.JwtProvider;
import ktb.fullstack.talktalk.global.resolver.LoginUserInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class StompAuthChannelInterceptorTest {

    @Mock
    SessionRepository sessionRepository;

    JwtProvider jwtProvider;
    StompAuthChannelInterceptor interceptor;

    @BeforeEach
    void setUp() {

        jwtProvider = new JwtProvider("test-secret-key-for-hmac-sha256-at-least-32-bytes-long-0123456789", 900);
        interceptor = new StompAuthChannelInterceptor(jwtProvider, sessionRepository);
    }

    private Message<byte[]> connectMessage(String authHeader) {

        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.setLeaveMutable(true);
        if (authHeader != null) {
            accessor.addNativeHeader("Authorization", authHeader);
        }
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }

    private Message<byte[]> userFrame(StompCommand command, Long sessionId) {

        StompHeaderAccessor accessor = StompHeaderAccessor.create(command);
        accessor.setLeaveMutable(true);
        accessor.setUser(new UsernamePasswordAuthenticationToken(new LoginUserInfo(1L, sessionId), null, List.of()));
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }

    private Message<byte[]> userFrameConnectedAt(StompCommand command, Long sessionId, long connectedAt) {

        StompHeaderAccessor accessor = StompHeaderAccessor.create(command);
        accessor.setLeaveMutable(true);
        accessor.setUser(new UsernamePasswordAuthenticationToken(new LoginUserInfo(1L, sessionId), null, List.of()));
        accessor.setSessionAttributes(Map.of("connectedAt", connectedAt));
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }

    @Test
    @DisplayName("유효한 토큰이면 CONNECT 프레임에 유저를 세팅한다")
    void 유효한_토큰이면_유저_세팅() {

        given(sessionRepository.existsById(2L)).willReturn(true);
        String token = jwtProvider.generateAccessToken(1L, 2L);
        Message<byte[]> message = connectMessage("Bearer " + token);

        interceptor.preSend(message, null);

        StompHeaderAccessor out = StompHeaderAccessor.wrap(message);
        assertThat(out.getUser()).isNotNull();
        LoginUserInfo sender = (LoginUserInfo) ((Authentication) out.getUser()).getPrincipal();
        assertThat(sender.userId()).isEqualTo(1L);
        assertThat(sender.sessionId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("인증 헤더가 없으면 예외를 던진다")
    void 인증_헤더_없으면_예외() {

        assertThatThrownBy(() -> interceptor.preSend(connectMessage(null), null))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.INVALID_TOKEN);
    }

    @Test
    @DisplayName("손상된 토큰이면 예외를 던진다")
    void 손상된_토큰이면_예외() {

        assertThatThrownBy(() -> interceptor.preSend(connectMessage("Bearer not-a-jwt"), null))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.INVALID_TOKEN);
    }

    @Test
    @DisplayName("세션이 존재하지 않으면 예외를 던진다")
    void 세션_없으면_예외() {

        given(sessionRepository.existsById(2L)).willReturn(false);
        String token = jwtProvider.generateAccessToken(1L, 2L);

        assertThatThrownBy(() -> interceptor.preSend(connectMessage("Bearer " + token), null))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.INVALID_TOKEN);
    }

    @Test
    @DisplayName("SEND 프레임 처리 시 세션이 살아있으면 통과한다")
    void SEND_세션_살아있으면_통과() {

        given(sessionRepository.existsById(2L)).willReturn(true);

        assertThatCode(() -> interceptor.preSend(userFrame(StompCommand.SEND, 2L), null))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("연결 중 세션이 폐기되면 SEND 프레임을 거부한다")
    void 세션_폐기되면_SEND_거부() {

        given(sessionRepository.existsById(2L)).willReturn(false);

        assertThatThrownBy(() -> interceptor.preSend(userFrame(StompCommand.SEND, 2L), null))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.INVALID_TOKEN);
    }

    @Test
    @DisplayName("연결 중 세션이 폐기되면 SUBSCRIBE 프레임을 거부한다")
    void 세션_폐기되면_SUBSCRIBE_거부() {

        given(sessionRepository.existsById(2L)).willReturn(false);

        assertThatThrownBy(() -> interceptor.preSend(userFrame(StompCommand.SUBSCRIBE, 2L), null))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.INVALID_TOKEN);
    }

    @Test
    @DisplayName("연결 수명 이내면 통과한다")
    void 수명_이내면_통과() {

        given(sessionRepository.existsById(2L)).willReturn(true);
        Message<byte[]> frame = userFrameConnectedAt(StompCommand.SEND, 2L, System.currentTimeMillis());

        assertThatCode(() -> interceptor.preSend(frame, null)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("연결 수명 TTL을 넘으면 거부한다")
    void 수명_초과면_거부() {

        long ago = System.currentTimeMillis() - (jwtProvider.getAccessTokenExpirationMillis() + 1);
        Message<byte[]> frame = userFrameConnectedAt(StompCommand.SEND, 2L, ago);

        assertThatThrownBy(() -> interceptor.preSend(frame, null))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.INVALID_TOKEN);
    }
}
