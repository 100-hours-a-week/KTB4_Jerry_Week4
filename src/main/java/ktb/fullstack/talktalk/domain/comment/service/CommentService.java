package ktb.fullstack.talktalk.domain.comment.service;

import ktb.fullstack.talktalk.domain.comment.entity.Comment;
import ktb.fullstack.talktalk.domain.comment.dto.request.CommentRequestDto;
import ktb.fullstack.talktalk.domain.comment.dto.response.CommentDto;
import ktb.fullstack.talktalk.domain.comment.dto.response.CommentListResponseDto;
import ktb.fullstack.talktalk.domain.comment.repository.CommentRepository;
import ktb.fullstack.talktalk.domain.post.entity.Post;
import ktb.fullstack.talktalk.domain.user.entity.User;
import ktb.fullstack.talktalk.domain.post.repository.PostRepository;
import ktb.fullstack.talktalk.domain.user.repository.UserRepository;
import ktb.fullstack.talktalk.domain.user.service.WriterResolver;
import ktb.fullstack.talktalk.global.common.response.CreateResponseDto;
import ktb.fullstack.talktalk.global.common.response.CursorPageResponse;
import ktb.fullstack.talktalk.global.exception.BusinessException;
import ktb.fullstack.talktalk.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

    private static final int PAGE_SIZE = 10;
    private static final int REPLY_PAGE_SIZE = 10;
    private static final String DELETED_CONTENT = "삭제된 댓글입니다.";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final WriterResolver writerResolver;

    /**
     *  1. 댓글 생성
     */
    @Transactional
    public CreateResponseDto createComment(Long postId, Long userId, CommentRequestDto request) {

        Post post = validateAndGetPost(postId);
        User user = userRepository.findById(userId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_TOKEN));

        Comment parentComment = resolveParent(postId, request.getParentId());
        Comment savedComment = commentRepository.save(new Comment(post, user, request.getContent(), parentComment));
        return new CreateResponseDto(savedComment.getId());
    }

    /**
     *  2. 댓글 목록 조회
     */
    @Transactional(readOnly = true)
    public CommentListResponseDto getComments(Long postId, Long cursor) {

        validateAndGetPost(postId);
        return new CommentListResponseDto(getTopLevelPage(postId, cursor));
    }

    /**
     *  2-1. 특정 댓글의 대댓글 목록 조회
     */
    @Transactional(readOnly = true)
    public CommentListResponseDto getReplies(Long postId, Long parentId, Long cursor) {

        validateAndGetPost(postId);

        Comment parent = commentRepository.findById(parentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));

        if (!parent.getPost().getId().equals(postId)) {
            throw new BusinessException(ErrorCode.COMMENT_NOT_FOUND);
        }

        return new CommentListResponseDto(getReplyPage(parentId, cursor));
    }

    /**
     *  2-2. 첫 페이지의 댓글만 조회
     */
    @Transactional(readOnly = true)
    public CursorPageResponse<CommentDto> getFirstPage(Long postId) {

        return getTopLevelPage(postId, null);
    }

    /**
     *  3. 댓글 내용 수정
     */
    @Transactional
    public CreateResponseDto updateComment(Long postId, Long commentId, Long userId, CommentRequestDto request) {

        Comment comment = validateAndFindComment(postId, commentId, userId);
        comment.update(request.getContent());
        return new CreateResponseDto(comment.getId());
    }

    /**
     *  4. 댓글 삭제
     */
    @Transactional
    public void deleteComment(Long postId, Long commentId, Long userId) {

        Comment comment = validateAndFindComment(postId, commentId, userId);

        if (commentRepository.existsByParentId(comment.getId())) {
            comment.softDelete();
            return;
        }

        Comment parent = comment.getParent();
        commentRepository.delete(comment);
        cleanupOrphanedParent(parent);
    }


    /* ===== 헬퍼 메소드 ===== */

    private Comment validateAndFindComment(Long postId, Long commentId, Long userId) {

        validateAndGetPost(postId);

        Comment comment = commentRepository.findByIdAndDeletedAtIsNull(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));

        if (!comment.getPost().getId().equals(postId)) {
            throw new BusinessException(ErrorCode.COMMENT_NOT_FOUND);
        }

        if (!comment.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOT_COMMENT_OWNER);
        }

        return comment;
    }

    private Post validateAndGetPost(Long postId) {

        return postRepository.findByIdAndDeletedAtIsNull(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
    }

    private CursorPageResponse<CommentDto> getReplyPage(Long parentId, Long cursor) {

        List<Comment> replies = commentRepository.findRepliesByCursor(
                parentId, cursor, PageRequest.of(0, REPLY_PAGE_SIZE + 1)
        );

        boolean hasNext = replies.size() > REPLY_PAGE_SIZE;
        List<Comment> repliesInPage = hasNext ? replies.subList(0, REPLY_PAGE_SIZE) : replies;
        Long nextCursor = hasNext ? replies.getLast().getId() : null;

        List<CommentDto> items = repliesInPage.stream()
                .map(this::toReplyDto)
                .toList();

        return new CursorPageResponse<>(items, nextCursor);
    }

    private void cleanupOrphanedParent(Comment parent) {

        if (parent == null || parent.getDeletedAt() == null) {
            return;
        }

        commentRepository.flush();
        if (!commentRepository.existsByParentId(parent.getId())) {
            commentRepository.delete(parent);
        }
    }


    private CommentDto toTopLevelDto(Comment comment) {

        boolean isDeleted = comment.getDeletedAt() != null;

        return new CommentDto(
                comment.getId(),
                isDeleted ? DELETED_CONTENT : comment.getContent(),
                comment.getCreatedAt().format(FORMATTER),
                writerResolver.resolveWriter(comment.getUser().getId()),
                isDeleted,
                getReplyPage(comment.getId(), null)
        );
    }

    private CommentDto toReplyDto(Comment comment) {

        return new CommentDto(
                comment.getId(),
                comment.getContent(),
                comment.getCreatedAt().format(FORMATTER),
                writerResolver.resolveWriter(comment.getUser().getId()),
                false,
                null
        );
    }

    private Comment resolveParent(Long postId, Long parentId) {

        if (parentId == null) {
            return null;
        }

        Comment parent = commentRepository.findByIdAndDeletedAtIsNull(parentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));

        if (!parent.getPost().getId().equals(postId)) {
            throw new BusinessException(ErrorCode.COMMENT_NOT_FOUND);
        }

        if (parent.isReply()) {
            throw new BusinessException(ErrorCode.CANNOT_REPLY_TO_REPLY);
        }

        return parent;
    }

    private CursorPageResponse<CommentDto> getTopLevelPage(Long postId, Long cursor) {

        List<Comment> tops = commentRepository.findTopLevelByCursor(
                postId, cursor, PageRequest.of(0, PAGE_SIZE + 1));

        boolean hasNext = tops.size() > PAGE_SIZE;
        List<Comment> commentsInPage = hasNext ? tops.subList(0, PAGE_SIZE) : tops;
        Long nextCursor = hasNext ? tops.getLast().getId() : null;

        List<CommentDto> items = commentsInPage.stream()
                .map(this::toTopLevelDto)
                .toList();

        return new CursorPageResponse<>(items, nextCursor);
    }
}