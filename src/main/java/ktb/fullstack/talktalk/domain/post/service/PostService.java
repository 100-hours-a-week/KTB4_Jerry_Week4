package ktb.fullstack.talktalk.domain.post.service;

import ktb.fullstack.talktalk.domain.image.entity.Image;
import ktb.fullstack.talktalk.domain.image.repository.ImageRepository;
import ktb.fullstack.talktalk.domain.post.dto.response.PostImageDto;
import ktb.fullstack.talktalk.domain.post.entity.PostHistory;
import ktb.fullstack.talktalk.domain.post.entity.PostImage;
import ktb.fullstack.talktalk.domain.post.port.CommentCountQuery;
import ktb.fullstack.talktalk.domain.comment.dto.response.CommentDto;
import ktb.fullstack.talktalk.domain.post.entity.Post;
import ktb.fullstack.talktalk.domain.post.dto.request.PostRequestDto;
import ktb.fullstack.talktalk.domain.post.dto.response.PostDetailResponseDto;
import ktb.fullstack.talktalk.domain.post.dto.response.PostSummaryDto;
import ktb.fullstack.talktalk.domain.post.dto.response.PostListResponseDto;
import ktb.fullstack.talktalk.domain.post.repository.PostHistoryRepository;
import ktb.fullstack.talktalk.domain.post.repository.PostImageRepository;
import ktb.fullstack.talktalk.domain.post.repository.PostLikeRepository;
import ktb.fullstack.talktalk.domain.user.dto.WriterDto;
import ktb.fullstack.talktalk.domain.post.port.CommentPageQuery;
import ktb.fullstack.talktalk.domain.post.repository.PostRepository;
import ktb.fullstack.talktalk.domain.user.entity.User;
import ktb.fullstack.talktalk.domain.user.repository.UserRepository;
import ktb.fullstack.talktalk.domain.user.service.WriterResolver;
import ktb.fullstack.talktalk.global.common.repository.CountByIdProjection;
import ktb.fullstack.talktalk.global.common.response.CreateResponseDto;
import ktb.fullstack.talktalk.global.common.response.CursorPageResponse;
import ktb.fullstack.talktalk.global.exception.BusinessException;
import ktb.fullstack.talktalk.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {

    private static final int PAGE_SIZE = 10;
    private static final String IMAGE_URL_PREFIX = "/images/";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String BLINDED_DESCRIPTION = "숨김 처리된 게시글입니다.";
    private static final int POST_RATE_LIMIT = 3;
    private static final Duration POST_RATE_WINDOW = Duration.ofMinutes(1);

    private final PostRepository postRepository;
    private final PostHistoryRepository postHistoryRepository;
    private final PostImageRepository postImageRepository;
    private final PostLikeRepository postLikeRepository;
    private final PostViewService postViewService;
    private final UserRepository userRepository;
    private final ImageRepository imageRepository;
    private final CommentCountQuery commentCountQuery;
    private final CommentPageQuery commentPageQuery;
    private final WriterResolver writerResolver;

    /**
     * 1. 게시글 목록 조회
     */
    @Transactional(readOnly = true)
    public PostListResponseDto getPosts(Long cursor) {
        List<Post> posts = postRepository.findByCursor(cursor, PageRequest.of(0, PAGE_SIZE + 1));

        boolean hasNext = posts.size() > PAGE_SIZE;
        List<Post> postsInPage = hasNext ? posts.subList(0, PAGE_SIZE) : posts;
        Long nextCursor = hasNext ? posts.getLast().getId() : null;

        if (postsInPage.isEmpty()) {
            return new PostListResponseDto(new CursorPageResponse<>(List.of(), nextCursor));
        }

        List<Long> postIds = postsInPage.stream().map(Post::getId).toList();
        List<Long> writerIds = postsInPage.stream()
                .map(post -> post.getUser().getId())
                .distinct()
                .toList();

        Map<Long, WriterDto> writers = writerResolver.resolveWriters(writerIds);
        Map<Long, Integer> likeCounts = postLikeRepository.countByPostIdIn(postIds).stream()
                .collect(Collectors.toMap(
                        CountByIdProjection::getId,
                        c -> (int) c.getTotal()));
        Map<Long, Integer> commentCounts = commentCountQuery.getCountsByPostIds(postIds);

        List<PostSummaryDto> summaries = postsInPage.stream()
                .map(post -> toSummary(post, writers, likeCounts, commentCounts))
                .toList();

        return new PostListResponseDto(new CursorPageResponse<>(summaries, nextCursor));
    }

    /**
     * 2. 게시글 생성
     */
    @Transactional
    public CreateResponseDto createPost(Long userId, PostRequestDto request) {

        validatePostRateLimit(userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_TOKEN));

        Post savedPost = postRepository.save(new Post(user, request.getTitle(), request.getContent()));
        savePostImages(savedPost, request.getPostImageIds());

        return new CreateResponseDto(savedPost.getId());
    }

    /**
     * 3. 게시글 상세 조회
     */
    @Transactional
    public PostDetailResponseDto getPostDetail(Long postId, Long userId) {

        Post post = postRepository.findByIdAndDeletedAtIsNull(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));

        if (postViewService.markViewed(post, userId)) {
            post.increaseViewCount();
        }

        WriterDto writerDto = writerResolver.resolveWriter(post.getUser().getId());
        boolean blinded = post.getBlindedAt() != null;
        CursorPageResponse<CommentDto> comments = commentPageQuery.getFirstPage(post.getId());

        return new PostDetailResponseDto(
                post.getId(),
                blinded ? BLINDED_DESCRIPTION : post.getTitle(),
                blinded ? BLINDED_DESCRIPTION : post.getContent(),
                blinded ? List.of() : getPostImages(post.getId()),
                postLikeRepository.existsByPostIdAndUserId(post.getId(), userId),
                postLikeRepository.countByPostId(post.getId()),
                commentCountQuery.getCountByPostId(post.getId()),
                post.getViewCount(),
                post.getCreatedAt().format(FORMATTER),
                writerDto,
                post.isEdited(),
                blinded,
                comments
        );
    }

    /**
     * 4. 게시글 수정
     */
    @Transactional
    public CreateResponseDto updatePost(Long postId, Long userId, PostRequestDto request) {

        Post post = postRepository.findByIdAndDeletedAtIsNull(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));

        if (!post.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOT_POST_OWNER);
        }

        List<Long> currentImageIds = postImageRepository.findImageIdsByPostId(postId);
        List<Long> newImageIds = request.getPostImageIds() == null ? List.of() : request.getPostImageIds();

        boolean textChanged = !post.getTitle().equals(request.getTitle())
                || !post.getContent().equals(request.getContent());
        boolean imagesChanged = !currentImageIds.equals(newImageIds);

        if (textChanged || imagesChanged) {
            int nextVersion = postHistoryRepository.countByPostId(post.getId()) + 1;
            postHistoryRepository.save(
                    new PostHistory(post, nextVersion, post.getTitle(), post.getContent(), currentImageIds));

            post.update(request.getTitle(), request.getContent());
            if (imagesChanged) {
                postImageRepository.deleteByPostId(postId);
                savePostImages(post, newImageIds);
            }
        }

        return new CreateResponseDto(post.getId());
    }

    /**
     * 5. 게시글 삭제
     */
    @Transactional
    public void deletePost(Long postId, Long userId) {

        Post post = postRepository.findByIdAndDeletedAtIsNull(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));

        if (!post.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOT_POST_OWNER);
        }

        post.softDelete();
    }


    /* ===== 헬퍼 메소드 ===== */

    private PostSummaryDto toSummary(Post post,
                                     Map<Long, WriterDto> writers,
                                     Map<Long, Integer> likeCounts,
                                     Map<Long, Integer> commentCounts) {

        boolean blinded = post.getBlindedAt() != null;

        return new PostSummaryDto(
                post.getId(),
                blinded ? BLINDED_DESCRIPTION : post.getTitle(),
                likeCounts.getOrDefault(post.getId(), 0),
                commentCounts.getOrDefault(post.getId(), 0),
                post.getViewCount(),
                post.getCreatedAt().format(FORMATTER),
                writers.get(post.getUser().getId()),
                post.isEdited(),
                blinded
        );
    }

    private void savePostImages(Post post, List<Long> imageIds) {

        if (imageIds == null || imageIds.isEmpty()) return;

        for (int i = 0; i < imageIds.size(); i++) {
            Image image = imageRepository.findById(imageIds.get(i))
                    .orElseThrow(() -> new BusinessException(ErrorCode.IMAGE_NOT_FOUND));
            postImageRepository.save(new PostImage(post, image, i));
        }
    }

    private void validatePostRateLimit(Long userId) {

        LocalDateTime windowStart = LocalDateTime.now().minus(POST_RATE_WINDOW);
        if (postRepository.countByUserIdAndCreatedAtAfter(userId, windowStart) >= POST_RATE_LIMIT) {
            throw new BusinessException(ErrorCode.TOO_MANY_POSTS_IN_SHORT);
        }
    }

    private List<PostImageDto> getPostImages(Long postId) {
        return postImageRepository.findByPostIdOrderBySortOrderAsc(postId).stream()
                .map(pi -> new PostImageDto(
                        pi.getImage().getId(), IMAGE_URL_PREFIX + pi.getImage().getFileName()))
                .toList();
    }
}
