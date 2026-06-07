package ktb.fullstack.talktalk.domain.user.repository;

import ktb.fullstack.talktalk.domain.user.domain.User;

import java.util.Optional;

public interface UserRepository {

    User save(User user);

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    Optional<User> findByEmail(String email);

    Optional<User> findById(Long id);

    void deleteById(Long id);
}
