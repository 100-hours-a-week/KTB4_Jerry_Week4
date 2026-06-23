package ktb.fullstack.talktalk.domain.post.repository;

import ktb.fullstack.talktalk.domain.post.entity.PostLike;
import ktb.fullstack.talktalk.global.common.repository.CountByIdProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    boolean existsByPostIdAndUserId(Long postId, Long userId);

    void deleteByPostIdAndUserId(Long postId, Long userId);

    int countByPostId(Long postId);

    @Query("""
            select pl.post.id as id, count(pl) as total
            from PostLike pl
            where pl.post.id in :postIds
            group by pl.post.id
            """
    )
    List<CountByIdProjection> countByPostIdIn(@Param("postIds") List<Long> postIds);
}
