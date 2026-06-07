package ktb.fullstack.talktalk.domain.image.repository;

import ktb.fullstack.talktalk.domain.image.domain.Image;

public interface ImageRepository {

    Image save(Image image);
}
