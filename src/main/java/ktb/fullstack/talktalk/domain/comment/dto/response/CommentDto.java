package ktb.fullstack.talktalk.domain.comment.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import ktb.fullstack.talktalk.domain.user.dto.WriterDto;
import ktb.fullstack.talktalk.global.common.response.CursorPageResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CommentDto {

    private Long id;

    private String content;

    @JsonProperty("created_at")
    private String createdAt;

    private WriterDto writer;

    @JsonProperty("is_deleted")
    private boolean deleted;

    private CursorPageResponse<CommentDto> replies;
}
