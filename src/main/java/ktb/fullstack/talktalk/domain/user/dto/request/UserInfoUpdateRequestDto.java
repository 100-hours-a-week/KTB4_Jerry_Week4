package ktb.fullstack.talktalk.domain.user.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class UserInfoUpdateRequestDto {

    @Size(max = 10, message = "닉네임은 최대 10자까지 작성 가능합니다.")
    @Pattern(
            regexp = "^\\S+$",
            message = "띄어쓰기를 없애주세요."
    )
    private String nickname;

    @Nullable
    @JsonProperty("profile_image_url")
    private String profileImageUrl;
}
