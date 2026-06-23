package ktb.fullstack.talktalk.domain.comment.adapter;

import ktb.fullstack.talktalk.domain.comment.repository.CommentRepository;
import ktb.fullstack.talktalk.domain.post.port.CommentCountQuery;
import ktb.fullstack.talktalk.global.common.repository.CountByIdProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CommentCountQueryAdapter implements CommentCountQuery {

    private final CommentRepository commentRepository;

    @Override
    public int getCountByPostId(Long postId) {

        return commentRepository.countByPostId(postId);
    }

    @Override
    public Map<Long, Integer> getCountsByPostIds(List<Long> postIds) {

        return commentRepository.countByPostIdIn(postIds).stream()
                .collect(Collectors.toMap(
                        CountByIdProjection::getId,
                        c -> (int) c.getTotal()));
    }
}
