package ktb.fullstack.talktalk.domain.user.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class UserSignupRequestDto {

    @NotBlank
    @Email(message = "올바른 이메일 주소 형식을 입력해주세요. (예: example@example.com)")
    private String email;

    @NotBlank
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-={}\\[\\]:;\"'<>,.?/]).{8,20}$",
            message = "비밀번호는 대문자, 소문자, 숫자, 특수문자를 각각 최소 1개 포함해야 합니다."
    )
    private String password;

    @NotBlank
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

