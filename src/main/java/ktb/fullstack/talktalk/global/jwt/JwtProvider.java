package ktb.fullstack.talktalk.global.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtProvider {

    private final SecretKey secretKey;
    private final long accessTokenExpirationMillis;

    public JwtProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-exp-seconds}") long accessTokenExpiration
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpirationMillis = accessTokenExpiration * 1000L;
    }

    public String generateAccessToken(Long userId, Long sessionId) {

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenExpirationMillis);

        return Jwts.builder()
                .issuer("ktb-jerry")
                .subject(String.valueOf(userId))
                .claim("sessionId", sessionId)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    public Jws<Claims> parse(String token) {

        return Jwts.parser()
                .verifyWith((SecretKey) secretKey)
                .build()
                .parseSignedClaims(token);
    }

    public Long getUserId(String token) {
        return Long.valueOf(parse(token).getPayload().getSubject());
    }

    public Long getSessionId(String token) {
        return parse(token).getPayload().get("sessionId", Long.class);
    }
}
