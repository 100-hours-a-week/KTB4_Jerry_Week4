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

    @JsonProperty("post_image_urls")
    private List<String> postImageUrls;

    @JsonProperty("updated_at")
    private String updatedAt;
}
