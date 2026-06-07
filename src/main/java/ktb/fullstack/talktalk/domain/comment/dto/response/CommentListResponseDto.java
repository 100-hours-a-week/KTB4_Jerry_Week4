package ktb.fullstack.talktalk.domain.comment.dto.response;

import ktb.fullstack.talktalk.global.common.response.CursorPageResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CommentListResponseDto {

    private CursorPageResponse<CommentDto> comments;
}
