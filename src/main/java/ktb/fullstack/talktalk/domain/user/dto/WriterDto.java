package ktb.fullstack.talktalk.domain.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class WriterDto {

    private Long id;

    private String nickname;

    @JsonProperty("profile_image_url")
    private String profileImageUrl;
}