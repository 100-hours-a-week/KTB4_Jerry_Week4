package ktb.fullstack.talktalk.domain.auth.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import ktb.fullstack.talktalk.domain.auth.dto.request.LoginRequestDto;
import ktb.fullstack.talktalk.domain.auth.dto.response.TokenResponseDto;
import ktb.fullstack.talktalk.domain.auth.service.AuthService;
import ktb.fullstack.talktalk.domain.auth.service.TokenResult;
import ktb.fullstack.talktalk.global.common.response.ApiResponse;
import ktb.fullstack.talktalk.global.resolver.LoginUser;
import ktb.fullstack.talktalk.global.resolver.LoginUserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sessions")
@RequiredArgsConstructor
public class SessionController {
    private final AuthService authService;

    @PostMapping
    public ResponseEntity<ApiResponse<TokenResponseDto>> login(
            @Valid @RequestBody LoginRequestDto request, HttpServletResponse response) {

        TokenResult result = authService.login(request);

        ResponseCookie cookie =
                buildCookieWithRefreshToken(result.getRefreshToken(), result.getRefreshTokenMaxAgeSeconds());
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.ok(ApiResponse.of("success", new TokenResponseDto(result.getAccessToken())));
    }

    @PostMapping("/current/token")
    public ResponseEntity<ApiResponse<TokenResponseDto>> refresh(
            @CookieValue(name = "refresh_token", required = false) String refreshToken,
            HttpServletResponse response) {

        TokenResult result = authService.refresh(refreshToken);

        ResponseCookie cookie =
                buildCookieWithRefreshToken(result.getRefreshToken(), result.getRefreshTokenMaxAgeSeconds());
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.ok(ApiResponse.of("success", new TokenResponseDto(result.getAccessToken())));
    }

    @DeleteMapping("/current")
    public ResponseEntity<ApiResponse<Void>> logout(@LoginUser LoginUserInfo loginUser, HttpServletResponse response) {
        authService.logout(loginUser.sessionId());

        ResponseCookie cookie = buildCookieWithRefreshToken("", 0);
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.ok(ApiResponse.of("success", null));
    }

    private ResponseCookie buildCookieWithRefreshToken(String refreshToken, long maxAgeSeconds) {
        return ResponseCookie.from("refresh_token", refreshToken)
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .path("/")
                .maxAge(maxAgeSeconds)
                .build();
    }
}
