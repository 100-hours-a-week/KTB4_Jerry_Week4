package ktb.fullstack.talktalk.domain.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TokenResponseDto {

    @JsonProperty("access_token")
    private final String accessToken;
}
