package ktb.fullstack.talktalk.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UserPasswordUpdateRequestDto (

    @NotBlank
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-={}\\[\\]:;\"'<>,.?/]).{8,20}$",
            message = "비밀번호는 대문자, 소문자, 숫자, 특수문자를 각각 최소 1개 포함해야 합니다."
    )
    String password
) {
}
