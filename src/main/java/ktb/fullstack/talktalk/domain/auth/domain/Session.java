package ktb.fullstack.talktalk.domain.auth.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Session {

    private Long id;

    private Long userId;

    private String refreshToken;

    private long expiresAt;

    public Session(Long userId, String refreshToken, long expiresAt) {
        this.userId = userId;
        this.refreshToken = refreshToken;
        this.expiresAt = expiresAt;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() > expiresAt;
    }

    public void renew(String newRefreshToken, long newExpiresAt) {
        this.refreshToken = newRefreshToken;
        this.expiresAt = newExpiresAt;
    }
}
