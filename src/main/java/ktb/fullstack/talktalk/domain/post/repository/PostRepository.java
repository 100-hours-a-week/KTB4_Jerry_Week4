package ktb.fullstack.talktalk.domain.post.repository;

import ktb.fullstack.talktalk.domain.post.domain.Post;

import java.util.List;
import java.util.Optional;

public interface PostRepository {
    List<Post> findByCursor(Long cursor, int size);

    Post save(Post post);

    Optional<Post> findById(Long id);

    void deleteById(Long id);
}
