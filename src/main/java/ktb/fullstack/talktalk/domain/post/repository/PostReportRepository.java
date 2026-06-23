package ktb.fullstack.talktalk.domain.post.repository;

import ktb.fullstack.talktalk.domain.post.entity.PostReport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostReportRepository extends JpaRepository<PostReport, Long> {

    boolean existsByPostIdAndUserId(Long postId, Long userId);

    int countByPostId(Long postId);
}
