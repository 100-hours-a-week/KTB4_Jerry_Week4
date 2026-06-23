package ktb.fullstack.talktalk.domain.post.service;

import ktb.fullstack.talktalk.domain.post.entity.Post;
import ktb.fullstack.talktalk.domain.post.entity.PostLike;
import ktb.fullstack.talktalk.domain.post.repository.PostLikeRepository;
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
public class PostLikeService {

    private final PostLikeRepository postLikeRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    /**
     *  1. 좋아요
     */
    @Transactional
    public void like(Long postId, Long userId) {

        Post post = postRepository.findByIdAndDeletedAtIsNull(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
        if (postLikeRepository.existsByPostIdAndUserId(postId, userId)) return;

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_TOKEN));

        postLikeRepository.save(new PostLike(post, user));
    }

    /**
     *  2. 좋아요 취소
     */
    @Transactional
    public void cancel(Long postId, Long userId) {

        postRepository.findByIdAndDeletedAtIsNull(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));

        postLikeRepository.deleteByPostIdAndUserId(postId, userId);
    }

}
