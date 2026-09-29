package ktb.fullstack.talktalk.domain.user.repository;

import ktb.fullstack.talktalk.domain.user.entity.ProfileImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProfileImageRepository extends JpaRepository<ProfileImage, Long> {

    @Query("""
            select pi
            from ProfileImage pi
            join fetch pi.image
            where pi.user.id = :userId and pi.current = true
           """)
    Optional<ProfileImage> findByUserIdAndCurrentTrue(@Param("userId") Long userId);

    Optional<ProfileImage> findByUserIdAndImageId(Long userId, Long imageId);

    @Query("""
            select pi
            from ProfileImage pi
            join fetch pi.image
            where pi.user.id in :userIds and pi.current = true
            """
    )
    List<ProfileImage> findByUserIdInAndCurrentTrue(@Param("userIds") List<Long> userIds);
}
