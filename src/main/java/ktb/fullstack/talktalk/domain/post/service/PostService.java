package ktb.fullstack.talktalk.domain.post.service;

import ktb.fullstack.talktalk.domain.post.port.CommentCountQuery;
import ktb.fullstack.talktalk.domain.comment.dto.response.CommentDto;
import ktb.fullstack.talktalk.domain.post.domain.Post;
import ktb.fullstack.talktalk.domain.post.dto.request.PostRequestDto;
import ktb.fullstack.talktalk.domain.post.dto.response.PostDetailResponseDto;
import ktb.fullstack.talktalk.domain.post.dto.response.PostSummaryDto;
import ktb.fullstack.talktalk.domain.post.dto.response.PostListResponseDto;
import ktb.fullstack.talktalk.domain.user.dto.WriterDto;
import ktb.fullstack.talktalk.domain.post.port.CommentPageQuery;
import ktb.fullstack.talktalk.domain.post.repository.PostRepository;
import ktb.fullstack.talktalk.domain.user.domain.User;
import ktb.fullstack.talktalk.domain.user.repository.UserRepository;
import ktb.fullstack.talktalk.domain.user.service.WriterResolver;
import ktb.fullstack.talktalk.global.common.response.CreateResponseDto;
import ktb.fullstack.talktalk.global.common.response.CursorPageResponse;
import ktb.fullstack.talktalk.global.exception.BusinessException;
import ktb.fullstack.talktalk.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {

    private static final int PAGE_SIZE = 10;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentCountQuery commentCountQuery;
    private final CommentPageQuery commentPageQuery;
    private final WriterResolver writerResolver;

    /**
     *  1. 게시글 목록 조회
     */
    public PostListResponseDto getPosts(Long cursor) {
        List<Post> posts = postRepository.findByCursor(cursor, PAGE_SIZE + 1);

        boolean hasNext = posts.size() > PAGE_SIZE;
        List<Post> postsInPage = hasNext ? posts.subList(0, PAGE_SIZE) : posts;
        Long nextCursor = hasNext ? posts.getLast().getId() : null;

        List<PostSummaryDto> summaries = postsInPage.stream()
                .map(this::toSummary)
                .toList();

        CursorPageResponse<PostSummaryDto> pageResponse = new CursorPageResponse<>(summaries, nextCursor);
        return new PostListResponseDto(pageResponse);
    }

    /**
     *  2. 게시글 생성
     */
    public CreateResponseDto createPost(Long userId, PostRequestDto request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_TOKEN));

        Post post = new Post(
                user.getId(),
                request.getTitle(),
                request.getContent(),
                request.getPostImageUrl()
        );

        Post savedPost = postRepository.save(post);
        return new CreateResponseDto(savedPost.getId());
    }

    /**
     *  3. 게시글 상세 조회
     */
    public PostDetailResponseDto getPostDetail(Long postId) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));

        post.increaseViewCount();
        postRepository.save(post);

        WriterDto writerDto = writerResolver.resolveWriter(post.getUserId());

        CursorPageResponse<CommentDto> comments = commentPageQuery.getFirstPage(post.getId());

        return new PostDetailResponseDto(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getPostImageUrl(),
                // 좋아요 수 관련 요구사항 없어서 구현 안하고 스텁으로 0 대체
                0,
                commentCountQuery.getCountByPostId(post.getId()),
                post.getViewCount(),
                post.getCreatedAt().format(FORMATTER),
                writerDto,
                comments
        );
    }

    /**
     *  4. 게시글 수정
     */
    public CreateResponseDto updatePost(Long postId, Long userId, PostRequestDto request) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));

        if (!post.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOT_POST_OWNER);
        }

        post.update(request.getTitle(), request.getContent(), request.getPostImageUrl());
        Post savedPost = postRepository.save(post);

        return new CreateResponseDto(savedPost.getId());
    }

    /**
     *  5. 게시글 삭제
     */
    public void deletePost(Long postId, Long userId) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));

        if (!post.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOT_POST_OWNER);
        }

        postRepository.deleteById(post.getId());
    }


    /* ===== 헬퍼 메소드 ===== */
    private PostSummaryDto toSummary(Post post) {

        WriterDto writerDto = writerResolver.resolveWriter(post.getUserId());

        return new PostSummaryDto(
                post.getId(),
                post.getTitle(),
                0,
                commentCountQuery.getCountByPostId(post.getId()),
                post.getViewCount(),
                post.getCreatedAt().format(FORMATTER),
                writerDto
        );
    }
}
