package ktb.fullstack.talktalk.domain.post.repository;

import ktb.fullstack.talktalk.domain.post.entity.Post;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    @Query("""
            select p from Post p
            where (:cursor is null or p.id <= :cursor) and p.deletedAt is null
            order by p.id desc
           """)
    List<Post> findByCursor(@Param("cursor") Long cursor, Pageable pageable);

    Optional<Post> findByIdAndDeletedAtIsNull(Long id);

    int countByUserIdAndCreatedAtAfter(Long userId, LocalDateTime createdAt);
}
