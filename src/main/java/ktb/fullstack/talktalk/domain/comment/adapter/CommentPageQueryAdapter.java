package ktb.fullstack.talktalk.domain.comment.adapter;

import ktb.fullstack.talktalk.domain.comment.dto.response.CommentDto;
import ktb.fullstack.talktalk.domain.comment.service.CommentService;
import ktb.fullstack.talktalk.domain.post.port.CommentPageQuery;
import ktb.fullstack.talktalk.global.common.response.CursorPageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CommentPageQueryAdapter implements CommentPageQuery {

    private final CommentService commentService;

    @Override
    public CursorPageResponse<CommentDto> getFirstPage(Long postId) {
        return commentService.getComments(postId, null).getComments();
    }
}
