package ktb.fullstack.talktalk.domain.comment.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class CommentRequestDto {

    @NotBlank
    private String content;

    @Nullable
    @JsonProperty("parent_id")
    private Long parentId;
}
