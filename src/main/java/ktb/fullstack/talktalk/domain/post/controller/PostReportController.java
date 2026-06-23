package ktb.fullstack.talktalk.domain.post.controller;

import ktb.fullstack.talktalk.domain.post.service.PostReportService;
import ktb.fullstack.talktalk.global.common.response.ApiResponse;
import ktb.fullstack.talktalk.global.resolver.LoginUser;
import ktb.fullstack.talktalk.global.resolver.LoginUserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/posts/{postId}/reports")
@RequiredArgsConstructor
public class PostReportController {

    private final PostReportService postReportService;

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> report(
            @PathVariable Long postId, @LoginUser LoginUserInfo loginUser) {

        postReportService.report(postId, loginUser.userId());
        return ResponseEntity.ok(ApiResponse.of("success", null));
    }
}
