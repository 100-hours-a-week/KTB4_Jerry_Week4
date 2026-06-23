package ktb.fullstack.talktalk.domain.post.controller;

import ktb.fullstack.talktalk.domain.post.service.PostLikeService;
import ktb.fullstack.talktalk.global.common.response.ApiResponse;
import ktb.fullstack.talktalk.global.resolver.LoginUser;
import ktb.fullstack.talktalk.global.resolver.LoginUserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/posts/{postId}/likes")
@RequiredArgsConstructor
public class PostLikeController {

    private final PostLikeService postLikeService;

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> like(
            @PathVariable Long postId, @LoginUser LoginUserInfo loginUser) {

        postLikeService.like(postId, loginUser.userId());
        return ResponseEntity.ok(ApiResponse.of("success", null));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> cancel(
            @PathVariable Long postId, @LoginUser LoginUserInfo loginUser) {

        postLikeService.cancel(postId, loginUser.userId());
        return ResponseEntity.ok(ApiResponse.of("success", null));
    }
}
