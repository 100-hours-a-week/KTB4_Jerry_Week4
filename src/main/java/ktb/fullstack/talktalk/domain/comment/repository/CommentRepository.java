package ktb.fullstack.talktalk.domain.comment.repository;

import ktb.fullstack.talktalk.domain.comment.entity.Comment;
import ktb.fullstack.talktalk.global.common.repository.CountByIdProjection;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("""
            select c from Comment c
            where c.post.id = :postId
                and c.parent.id is null
                and (:cursor is null or c.id >= :cursor)
            order by c.id asc
            """)
    List<Comment> findTopLevelByCursor(@Param("postId") Long postId,
                                       @Param("cursor") Long cursor,
                                       Pageable pageable);


    @Query("""
            select c from Comment c
            where c.parent.id = :parentId
                and (:cursor is null or c.id >= :cursor)
            order by c.id asc
            """)
    List<Comment> findRepliesByCursor(@Param("parentId") Long parentId,
                                      @Param("cursor") Long cursor,
                                      Pageable pageable);

    boolean existsByParentId(Long parentId);

    int countByPostId(Long postId);

    Optional<Comment> findByIdAndDeletedAtIsNull(Long id);

    @Query("""
            select c.post.id as id, count(c) as total
            from Comment c
            where c.post.id in :postIds
            group by c.post.id
            """)
    List<CountByIdProjection> countByPostIdIn(@Param("postIds") List<Long> postIds);
}
