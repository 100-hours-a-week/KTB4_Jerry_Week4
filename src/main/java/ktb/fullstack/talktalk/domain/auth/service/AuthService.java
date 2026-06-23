package ktb.fullstack.talktalk.domain.auth.service;

import ktb.fullstack.talktalk.domain.auth.entity.Session;
import ktb.fullstack.talktalk.domain.auth.dto.request.LoginRequestDto;
import ktb.fullstack.talktalk.domain.auth.repository.SessionRepository;
import ktb.fullstack.talktalk.domain.user.entity.User;
import ktb.fullstack.talktalk.domain.user.repository.UserRepository;
import ktb.fullstack.talktalk.global.exception.BusinessException;
import ktb.fullstack.talktalk.global.exception.ErrorCode;
import ktb.fullstack.talktalk.global.jwt.JwtProvider;
import ktb.fullstack.talktalk.global.jwt.RefreshTokenGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

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
    @Transactional
    public TokenResult login(LoginRequestDto request) {

        User user = userRepository.findByEmailAndDeletedAtIsNull(request.getEmail())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));

        if (!user.getPassword().equals(request.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        String refreshToken = refreshTokenGenerator.generate();
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(refreshTokenExpSeconds);
        Session session = sessionRepository.save(new Session(user, refreshToken, expiresAt));
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
        LocalDateTime newExpiresAt = LocalDateTime.now().plusSeconds(refreshTokenExpSeconds);
        session.renew(newRefreshToken, newExpiresAt);
        sessionRepository.save(session);

        String newAccessToken = jwtProvider.generateAccessToken(session.getUser().getId(), session.getId());
        return new TokenResult(newAccessToken, newRefreshToken, refreshTokenExpSeconds);
    }

    /**
     *  3. 로그아웃 (세션 삭제)
     */
    public void logout(Long sessionId) {
        sessionRepository.deleteById(sessionId);
    }
}
