package ktb.fullstack.talktalk.domain.auth.repository;

import ktb.fullstack.talktalk.domain.auth.domain.Session;

import java.util.Optional;

public interface SessionRepository {

    Session save(Session session);

    Optional<Session> findByRefreshToken(String refreshToken);

    void deleteById(Long id);

    void deleteByUserId(Long userId);
}
