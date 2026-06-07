package ktb.fullstack.talktalk.domain.user.controller;

import jakarta.validation.Valid;
import ktb.fullstack.talktalk.domain.user.dto.request.UserInfoUpdateRequestDto;
import ktb.fullstack.talktalk.domain.user.dto.request.UserPasswordUpdateRequestDto;
import ktb.fullstack.talktalk.domain.user.dto.request.UserSignupRequestDto;
import ktb.fullstack.talktalk.domain.user.dto.response.AvailabilityResponseDto;
import ktb.fullstack.talktalk.domain.user.dto.response.UserInfoResponseDto;
import ktb.fullstack.talktalk.domain.user.service.UserService;
import ktb.fullstack.talktalk.global.common.response.ApiResponse;
import ktb.fullstack.talktalk.global.common.response.CreateResponseDto;
import ktb.fullstack.talktalk.global.resolver.LoginUser;
import ktb.fullstack.talktalk.global.resolver.LoginUserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<ApiResponse<CreateResponseDto>> createUser(@Valid @RequestBody UserSignupRequestDto request) {

        CreateResponseDto result = userService.createUser(request);

        URI location = UriComponentsBuilder.fromPath("/users/{id}")
                .buildAndExpand(result.getId())
                .toUri();

        return ResponseEntity.created(location).body(ApiResponse.of("success", result));
    }

    @GetMapping("/email-availability")
    public ResponseEntity<ApiResponse<AvailabilityResponseDto>> checkEmailAvailability(@RequestParam String email) {

        AvailabilityResponseDto result = userService.checkEmailAvailability(email);
        return ResponseEntity.ok(ApiResponse.of("success", result));
    }

    @GetMapping("/nickname-availability")
    public ResponseEntity<ApiResponse<AvailabilityResponseDto>> checkNicknameAvailability(@RequestParam String nickname) {

        AvailabilityResponseDto result = userService.checkNicknameAvailability(nickname);
        return ResponseEntity.ok(ApiResponse.of("success", result));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserInfoResponseDto>> getMyInfo(@LoginUser LoginUserInfo loginUser) {

        UserInfoResponseDto result = userService.getMyInfo(loginUser.userId());
        return ResponseEntity.ok(ApiResponse.of("success", result));
    }

    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<UserInfoResponseDto>> updateMyInfo(
            @LoginUser LoginUserInfo loginUser, @Valid @RequestBody UserInfoUpdateRequestDto request) {

        UserInfoResponseDto result = userService.updateMyInfo(loginUser.userId(), request);
        return ResponseEntity.ok(ApiResponse.of("success", result));
    }

    @PatchMapping("/me/password")
    public ResponseEntity<ApiResponse<Void>> updatePassword(
            @LoginUser LoginUserInfo loginUser, @Valid @RequestBody UserPasswordUpdateRequestDto request) {

        userService.updatePassword(loginUser.userId(), request);
        return ResponseEntity.ok(ApiResponse.of("success", null));
    }

    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<Void>> deleteMyAccount(@LoginUser LoginUserInfo loginUser) {
        userService.deleteMyAccount(loginUser.userId());
        return ResponseEntity.ok(ApiResponse.of("success", null));
    }
}
