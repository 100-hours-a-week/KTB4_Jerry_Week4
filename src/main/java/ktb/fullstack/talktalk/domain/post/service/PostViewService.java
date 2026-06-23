package ktb.fullstack.talktalk.domain.post.service;

import ktb.fullstack.talktalk.domain.post.entity.Post;
import ktb.fullstack.talktalk.domain.post.entity.PostView;
import ktb.fullstack.talktalk.domain.post.repository.PostViewRepository;
import ktb.fullstack.talktalk.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PostViewService {

    private static final Duration VIEW_COUNT_COOLDOWN = Duration.ofHours(24);

    private final PostViewRepository postViewRepository;
    private final UserRepository userRepository;

    @Transactional
    public boolean markViewed(Post post, Long userId) {

        LocalDateTime now = LocalDateTime.now();

        return postViewRepository.findByPostIdAndUserId(post.getId(), userId)
                .map(view -> {
                    if (view.getLastViewedAt().isBefore(now.minus(VIEW_COUNT_COOLDOWN))) {
                        view.updateLastViewedAt();
                        return true;
                    }

                    return false;
                })
                .orElseGet(() -> {
                    postViewRepository.save(new PostView(post, userRepository.getReferenceById(userId)));
                    return true;
                });
    }
}
