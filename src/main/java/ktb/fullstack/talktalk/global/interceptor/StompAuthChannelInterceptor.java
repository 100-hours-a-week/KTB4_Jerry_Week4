package ktb.fullstack.talktalk.global.interceptor;

import io.jsonwebtoken.JwtException;
import ktb.fullstack.talktalk.domain.auth.repository.SessionRepository;
import ktb.fullstack.talktalk.global.exception.BusinessException;
import ktb.fullstack.talktalk.global.exception.ErrorCode;
import ktb.fullstack.talktalk.global.jwt.JwtProvider;
import ktb.fullstack.talktalk.global.resolver.LoginUserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String CONNECTED_AT = "connectedAt";

    private final JwtProvider jwtProvider;
    private final SessionRepository sessionRepository;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) return message;

        StompCommand command = accessor.getCommand();
        if (StompCommand.CONNECT.equals(command)) {
            accessor.setUser(authenticate(accessor));
            markConnectedAt(accessor);

        } else if (StompCommand.SEND.equals(command) || StompCommand.SUBSCRIBE.equals(command)) {
            verifyWithinLifetime(accessor);
            verifySessionAlive(accessor);
        }
        return message;
    }

    private Authentication authenticate(StompHeaderAccessor accessor) {

        String header = accessor.getFirstNativeHeader("Authorization");
        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        String token = header.substring(BEARER_PREFIX.length());
        try {
            Long userId = jwtProvider.getUserId(token);
            Long sessionId = jwtProvider.getSessionId(token);
            if (!sessionRepository.existsById(sessionId)) {
                throw new BusinessException(ErrorCode.INVALID_TOKEN);
            }

            LoginUserInfo principal = new LoginUserInfo(userId, sessionId);
            return new UsernamePasswordAuthenticationToken(principal, null, List.of());

        } catch (JwtException | IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }
    }

    private void verifySessionAlive(StompHeaderAccessor accessor) {

        Principal user = accessor.getUser();
        if (user == null) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        LoginUserInfo info = (LoginUserInfo) ((Authentication) user).getPrincipal();
        if (!sessionRepository.existsById(info.sessionId())) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }
    }

    private void markConnectedAt(StompHeaderAccessor accessor) {

        Map<String, Object> attributes = accessor.getSessionAttributes();
        if (attributes != null) {
            attributes.put(CONNECTED_AT, System.currentTimeMillis());
        }
    }

    private void verifyWithinLifetime(StompHeaderAccessor accessor) {

        Map<String, Object> attributes = accessor.getSessionAttributes();
        if (attributes == null) return;

        if (attributes.get(CONNECTED_AT) instanceof Long connectedAt
                && System.currentTimeMillis() - connectedAt > jwtProvider.getAccessTokenExpirationMillis()) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }
    }
}
