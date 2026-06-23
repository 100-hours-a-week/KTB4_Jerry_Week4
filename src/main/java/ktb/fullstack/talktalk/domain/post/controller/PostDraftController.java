package ktb.fullstack.talktalk.domain.post.controller;

import jakarta.validation.Valid;
import ktb.fullstack.talktalk.domain.post.dto.request.PostDraftRequestDto;
import ktb.fullstack.talktalk.domain.post.dto.response.PostDraftResponseDto;
import ktb.fullstack.talktalk.domain.post.service.PostDraftService;
import ktb.fullstack.talktalk.global.common.response.ApiResponse;
import ktb.fullstack.talktalk.global.resolver.LoginUser;
import ktb.fullstack.talktalk.global.resolver.LoginUserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/posts/draft")
@RequiredArgsConstructor
public class PostDraftController {

    private final PostDraftService postDraftService;

    @PutMapping
    public ResponseEntity<ApiResponse<Void>> save(
            @LoginUser LoginUserInfo loginUser, @Valid @RequestBody PostDraftRequestDto request) {

        postDraftService.save(loginUser.userId(), request);
        return ResponseEntity.ok(ApiResponse.of("success", null));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PostDraftResponseDto>> getMyDraft(@LoginUser LoginUserInfo loginUser) {

        PostDraftResponseDto result = postDraftService.getMyDraft(loginUser.userId());
        return ResponseEntity.ok(ApiResponse.of("success", result));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> delete(@LoginUser LoginUserInfo loginUser) {

        postDraftService.delete(loginUser.userId());
        return ResponseEntity.ok(ApiResponse.of("success", null));
    }
}
