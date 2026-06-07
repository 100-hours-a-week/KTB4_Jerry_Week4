package ktb.fullstack.talktalk.domain.comment.service;

import ktb.fullstack.talktalk.domain.comment.domain.Comment;
import ktb.fullstack.talktalk.domain.comment.dto.request.CommentRequestDto;
import ktb.fullstack.talktalk.domain.comment.dto.response.CommentDto;
import ktb.fullstack.talktalk.domain.comment.dto.response.CommentListResponseDto;
import ktb.fullstack.talktalk.domain.comment.repository.CommentRepository;
import ktb.fullstack.talktalk.domain.user.dto.WriterDto;
import ktb.fullstack.talktalk.domain.post.repository.PostRepository;
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
public class CommentService {

    private static final int PAGE_SIZE = 10;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final WriterResolver writerResolver;

    /**
     *  1. 댓글 생성
     */
    public CreateResponseDto createComment(Long postId, Long userId, CommentRequestDto request) {

        validatePostExists(postId);

        Comment comment = new Comment(postId, userId, request.getContent());
        Comment savedComment = commentRepository.save(comment);
        return new CreateResponseDto(savedComment.getId());
    }

    /**
     *  2. 댓글 목록 조회
     */
    public CommentListResponseDto getComments(Long postId, Long cursor) {

        validatePostExists(postId);

        List<Comment> comments = commentRepository.findByCursor(postId, cursor, PAGE_SIZE + 1);
        boolean hasNext = comments.size() > PAGE_SIZE;

        List<Comment> commentsInPage = hasNext ? comments.subList(0, PAGE_SIZE) : comments;
        Long nextCursor = hasNext ? comments.getLast().getId() : null;

        List<CommentDto> items = commentsInPage.stream()
                .map(this::toCommentDto)
                .toList();

        CursorPageResponse<CommentDto> pageResponse = new CursorPageResponse<>(items, nextCursor);
        return new CommentListResponseDto(pageResponse);
    }

    /**
     *  3. 댓글 내용 수정
     */
    public CreateResponseDto updateComment(Long postId, Long commentId, Long userId, CommentRequestDto request) {

        Comment comment = validateAndFindComment(postId, commentId, userId);
        comment.update(request.getContent());
        Comment savedComment = commentRepository.save(comment);
        return new CreateResponseDto(savedComment.getId());
    }

    /**
     *  4. 댓글 삭제
     */
    public void deleteComment(Long postId, Long commentId, Long userId) {

        Comment comment = validateAndFindComment(postId, commentId, userId);
        commentRepository.deleteById(comment.getId());
    }


    /* ===== 헬퍼 메소드 ===== */

    private Comment validateAndFindComment(Long postId, Long commentId, Long userId) {
        validatePostExists(postId);

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));

        if (!comment.getPostId().equals(postId)) {
            throw new BusinessException(ErrorCode.COMMENT_NOT_FOUND);
        }

        if (!comment.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOT_COMMENT_OWNER);
        }

        return comment;
    }

    private void validatePostExists(Long postId) {
        postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
    }

    private CommentDto toCommentDto(Comment comment) {

        WriterDto writerDto = writerResolver.resolveWriter(comment.getUserId());

        return new CommentDto(
                comment.getId(),
                comment.getContent(),
                comment.getCreatedAt().format(FORMATTER),
                writerDto
        );
    }
}