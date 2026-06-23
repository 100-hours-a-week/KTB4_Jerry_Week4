package ktb.fullstack.talktalk.domain.post.repository;

import ktb.fullstack.talktalk.domain.post.entity.PostDraft;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostDraftRepository extends JpaRepository<PostDraft, Long> {

    Optional<PostDraft> findByUserId(Long userId);
}
