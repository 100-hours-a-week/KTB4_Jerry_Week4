package ktb.fullstack.talktalk.domain.post.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class PostDraftResponseDto {

    private String title;

    private String content;

    @JsonProperty("post_images")
    private List<PostImageDto> postImages;

    @JsonProperty("updated_at")
    private String updatedAt;
}
