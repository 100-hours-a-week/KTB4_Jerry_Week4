package ktb.fullstack.talktalk.domain.post.repository;

import ktb.fullstack.talktalk.domain.post.entity.PostImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostImageRepository extends JpaRepository<PostImage, Long> {

    List<PostImage> findByPostIdOrderBySortOrderAsc(Long postId);

    @Modifying
    @Query("delete from PostImage pi where pi.post.id = :postId")
    void deleteByPostId(@Param("postId") Long postId);

    @Query("select pi.image.id from PostImage pi where pi.post.id = :postId order by pi.sortOrder asc")
    List<Long> findImageIdsByPostId(@Param("postId") Long postId);
}
