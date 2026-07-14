package ktb.fullstack.talktalk.domain.comment.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CommentCreateResponseDto {

    private final Long id;

    @JsonProperty("created_at")
    private final String createdAt;
}
