package ktb.fullstack.talktalk.domain.comment.adapter;

import ktb.fullstack.talktalk.domain.comment.repository.CommentRepository;
import ktb.fullstack.talktalk.domain.post.port.CommentCountQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CommentCountQueryAdapter implements CommentCountQuery {

    private final CommentRepository commentRepository;

    @Override
    public int getCountByPostId(Long postId) {

        return commentRepository.getCountByPostId(postId);
    }
}
