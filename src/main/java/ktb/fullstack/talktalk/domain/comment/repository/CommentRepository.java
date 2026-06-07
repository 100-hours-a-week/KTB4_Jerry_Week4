package ktb.fullstack.talktalk.domain.comment.repository;

import ktb.fullstack.talktalk.domain.comment.domain.Comment;

import java.util.List;
import java.util.Optional;

public interface CommentRepository {

    Optional<Comment> findById(Long id);

    Comment save(Comment comment);

    List<Comment> findByCursor(Long postId, Long cursor, int size);

    void deleteById(Long id);

    int getCountByPostId(Long postId);
}
