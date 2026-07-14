package ktb.fullstack.talktalk.domain.comment.controller;

import jakarta.validation.Valid;
import ktb.fullstack.talktalk.domain.comment.dto.request.CommentRequestDto;
import ktb.fullstack.talktalk.domain.comment.dto.response.CommentCreateResponseDto;
import ktb.fullstack.talktalk.domain.comment.dto.response.CommentListResponseDto;
import ktb.fullstack.talktalk.domain.comment.service.CommentService;
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
@RequestMapping("/posts/{postId}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<ApiResponse<CommentCreateResponseDto>> createComment(
            @PathVariable Long postId,
            @LoginUser LoginUserInfo loginUser,
            @Valid @RequestBody CommentRequestDto request) {

        CommentCreateResponseDto result = commentService.createComment(postId, loginUser.userId(), request);

        URI location = UriComponentsBuilder.fromPath("/posts/{postId}/comments/{commentId}")
                .buildAndExpand(postId, result.getId())
                .toUri();

        return ResponseEntity.created(location).body(ApiResponse.of("success", result));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<CommentListResponseDto>> getComments(
            @PathVariable Long postId, @RequestParam(required = false) Long cursor) {

        CommentListResponseDto result = commentService.getComments(postId, cursor);
        return ResponseEntity.ok(ApiResponse.of("success", result));
    }

    @GetMapping("/{commentId}/replies")
    public ResponseEntity<ApiResponse<CommentListResponseDto>> getReplies(
            @PathVariable Long postId, @PathVariable Long commentId, @RequestParam(required = false) Long cursor) {

        CommentListResponseDto result = commentService.getReplies(postId, commentId, cursor);
        return ResponseEntity.ok(ApiResponse.of("success", result));
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<ApiResponse<CreateResponseDto>> updateComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @LoginUser LoginUserInfo loginUser,
            @Valid @RequestBody CommentRequestDto request) {

        CreateResponseDto result = commentService.updateComment(postId, commentId, loginUser.userId(), request);
        return ResponseEntity.ok(ApiResponse.of("success", result));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @PathVariable Long postId, @PathVariable Long commentId, @LoginUser LoginUserInfo loginUser) {

        commentService.deleteComment(postId, commentId, loginUser.userId());
        return ResponseEntity.ok(ApiResponse.of("success", null));
    }
}
