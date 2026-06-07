package ktb.fullstack.talktalk.domain.post.dto.response;

import ktb.fullstack.talktalk.global.common.response.CursorPageResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PostListResponseDto {

    private CursorPageResponse<PostSummaryDto> posts;
}
