package ktb.fullstack.talktalk.domain.post.service;

import ktb.fullstack.talktalk.domain.post.entity.Post;
import ktb.fullstack.talktalk.domain.post.entity.PostReport;
import ktb.fullstack.talktalk.domain.post.repository.PostReportRepository;
import ktb.fullstack.talktalk.domain.post.repository.PostRepository;
import ktb.fullstack.talktalk.domain.user.entity.User;
import ktb.fullstack.talktalk.domain.user.repository.UserRepository;
import ktb.fullstack.talktalk.global.exception.BusinessException;
import ktb.fullstack.talktalk.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostReportService {

    private static final int BLIND_THRESHOLD = 5;

    private final PostReportRepository postReportRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    /**
     *  게시글 신고
     */
    @Transactional
    public void report(Long postId, Long userId) {

        if (postReportRepository.existsByPostIdAndUserId(postId, userId)) {
            throw new BusinessException(ErrorCode.ALREADY_REPORTED);
        }

        Post post = postRepository.findByIdAndDeletedAtIsNull(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_TOKEN));

        postReportRepository.save(new PostReport(post, user));

        if (post.getBlindedAt() == null &&
                postReportRepository.countByPostId(postId) >= BLIND_THRESHOLD) {
            post.blind();
        }
    }
}
