package ktb.fullstack.talktalk.domain.image.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ImageUploadResponseDto {

    private Long id;

    @JsonProperty("image_url")
    private String imageUrl;
}
