package ktb.fullstack.talktalk.domain.image.repository;

import ktb.fullstack.talktalk.domain.image.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageRepository extends JpaRepository<Image, Long> {
}
