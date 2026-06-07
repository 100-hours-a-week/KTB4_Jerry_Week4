package ktb.fullstack.talktalk.domain.auth.service;

import ktb.fullstack.talktalk.domain.auth.domain.Session;
import ktb.fullstack.talktalk.domain.auth.dto.request.LoginRequestDto;
import ktb.fullstack.talktalk.domain.auth.repository.SessionRepository;
import ktb.fullstack.talktalk.domain.user.domain.User;
import ktb.fullstack.talktalk.domain.user.repository.UserRepository;
import ktb.fullstack.talktalk.global.exception.BusinessException;
import ktb.fullstack.talktalk.global.exception.ErrorCode;
import ktb.fullstack.talktalk.global.jwt.JwtProvider;
import ktb.fullstack.talktalk.global.jwt.RefreshTokenGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final RefreshTokenGenerator refreshTokenGenerator;
    private final JwtProvider jwtProvider;

    @Value("${jwt.refresh-token-exp-seconds}")
    private long refreshTokenExpSeconds;

    /**
     * 1. 로그인 (세션 & JWT 생성)
     */
    public TokenResult login(LoginRequestDto request) {
        String email = request.getEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));

        String password = request.getPassword();
        if (!user.getPassword().equals(password)) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        String refreshToken = refreshTokenGenerator.generate();
        long expiresAt = System.currentTimeMillis() + refreshTokenExpSeconds * 1000L;
        Session session = sessionRepository.save(new Session(user.getId(), refreshToken, expiresAt));
        String accessToken = jwtProvider.generateAccessToken(user.getId(), session.getId());

        return new TokenResult(accessToken, refreshToken, refreshTokenExpSeconds);
    }

    /**
     *  2. 액세스토큰 및 리프레시토큰 재발급
     *     - RTR 전략 사용
     */
    public TokenResult refresh(String refreshToken) {
        if (refreshToken == null) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        Session session = sessionRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_TOKEN));

        if (session.isExpired()) {
            sessionRepository.deleteById(session.getId());
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        String newRefreshToken = refreshTokenGenerator.generate();
        long newExpiresAt = System.currentTimeMillis() + refreshTokenExpSeconds * 1000L;
        session.renew(newRefreshToken, newExpiresAt);
        sessionRepository.save(session);

        String newAccessToken = jwtProvider.generateAccessToken(session.getUserId(), session.getId());

        return new TokenResult(newAccessToken, newRefreshToken, refreshTokenExpSeconds);
    }

    /**
     *  3. 로그아웃 (세션 삭제)
     */
    public void logout(Long sessionId) {
        sessionRepository.deleteById(sessionId);
    }
}
