package ktb.fullstack.talktalk.domain.user.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserInfoResponseDto {

    private Long id;

    private String email;

    private String nickname;

    @JsonProperty("profile_image_url")
    private String profileImageUrl;
}
