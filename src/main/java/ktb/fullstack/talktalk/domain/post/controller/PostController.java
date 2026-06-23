package ktb.fullstack.talktalk.domain.post.controller;

import jakarta.validation.Valid;
import ktb.fullstack.talktalk.domain.post.dto.request.PostRequestDto;
import ktb.fullstack.talktalk.domain.post.dto.response.PostDetailResponseDto;
import ktb.fullstack.talktalk.domain.post.dto.response.PostListResponseDto;
import ktb.fullstack.talktalk.domain.post.service.PostService;
import ktb.fullstack.talktalk.global.common.response.ApiResponse;
import ktb.fullstack.talktalk.global.common.response.CreateResponseDto;
import ktb.fullstack.talktalk.global.resolver.LoginUser;
import ktb.fullstack.talktalk.global.resolver.LoginUserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;


@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @GetMapping
    public ResponseEntity<ApiResponse<PostListResponseDto>> getPosts(@RequestParam(required = false) Long cursor) {

        PostListResponseDto result = postService.getPosts(cursor);
        return ResponseEntity.ok(ApiResponse.of("success", result));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CreateResponseDto>> createPost(
            @LoginUser LoginUserInfo loginUser,
            @Valid @RequestBody PostRequestDto request) {

        CreateResponseDto result = postService.createPost(loginUser.userId(), request);

        URI location = UriComponentsBuilder.fromPath("/posts/{id}")
                .buildAndExpand(result.getId())
                .toUri();

        return ResponseEntity.created(location).body(ApiResponse.of("success", result));
    }

    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostDetailResponseDto>> getPostDetail(
            @PathVariable Long postId, @LoginUser LoginUserInfo loginUser) {

        PostDetailResponseDto result = postService.getPostDetail(postId, loginUser.userId());
        return ResponseEntity.ok(ApiResponse.of("success", result));
    }

    @PutMapping("/{postId}")
    public ResponseEntity<ApiResponse<CreateResponseDto>> updatePost(
            @PathVariable Long postId, @LoginUser LoginUserInfo loginUser,
            @Valid @RequestBody PostRequestDto request) {

        CreateResponseDto result = postService.updatePost(postId, loginUser.userId(), request);
        return ResponseEntity.ok(ApiResponse.of("success", result));
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<ApiResponse<Void>> deletePost(@PathVariable Long postId, @LoginUser LoginUserInfo loginUser) {

        postService.deletePost(postId, loginUser.userId());
        return ResponseEntity.ok(ApiResponse.of("success", null));
    }
}