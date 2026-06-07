package ktb.fullstack.talktalk.domain.post.port;

import ktb.fullstack.talktalk.domain.comment.dto.response.CommentDto;
import ktb.fullstack.talktalk.global.common.response.CursorPageResponse;

public interface CommentPageQuery {

    CursorPageResponse<CommentDto> getFirstPage(Long postId);
}
