package ktb.fullstack.talktalk.domain.post.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.util.List;

@Getter
public class PostRequestDto {

    @NotBlank
    @Size(max = 26)
    private String title;

    @NotBlank
    private String content;

    @Nullable
    @JsonProperty("post_image_ids")
    private List<Long> postImageIds;
}
