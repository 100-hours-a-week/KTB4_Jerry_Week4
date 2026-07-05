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
import ktb.fullstack.talktalk.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final RefreshTokenGenerator refreshTokenGenerator;
    private final JwtProvider jwtProvider;
    private final AuthenticationManager authenticationManager;

    @Value("${jwt.refresh-token-exp-seconds}")
    private long refreshTokenExpSeconds;

    @Transactional
    public TokenResult login(LoginRequestDto request) {

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (AuthenticationException e) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();
        Long userId = Objects.requireNonNull(principal).getUserId();

        User user = userRepository.getReferenceById(userId);

        String refreshToken = refreshTokenGenerator.generate();
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(refreshTokenExpSeconds);
        Session session = sessionRepository.save(new Session(user, refreshToken, expiresAt));
        String accessToken = jwtProvider.generateAccessToken(user.getId(), session.getId());

        return new TokenResult(accessToken, refreshToken, refreshTokenExpSeconds);
    }

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

    public void logout(Long sessionId) {
        sessionRepository.deleteById(sessionId);
    }
}
