package ktb.fullstack.talktalk.domain.auth.service;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TokenResult {

    private String accessToken;

    private String refreshToken;

    private long refreshTokenMaxAgeSeconds;
}
