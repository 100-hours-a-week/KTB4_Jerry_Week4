package ktb.fullstack.talktalk.domain.post.repository;

import ktb.fullstack.talktalk.domain.post.entity.PostHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostHistoryRepository extends JpaRepository<PostHistory, Long> {

     int countByPostId(Long postId);

     List<PostHistory> findByPostIdOrderByVersionDesc(Long postId);
}
