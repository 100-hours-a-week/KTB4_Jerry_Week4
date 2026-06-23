package ktb.fullstack.talktalk.domain.auth.repository;

import ktb.fullstack.talktalk.domain.auth.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SessionRepository extends JpaRepository<Session, Long> {

    Optional<Session> findByRefreshToken(String refreshToken);

    void deleteByUserId(Long userId);
}
