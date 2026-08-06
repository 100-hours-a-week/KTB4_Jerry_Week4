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
import org.springframework.security.core.Authentication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
}
