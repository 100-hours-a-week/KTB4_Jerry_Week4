package ktb.fullstack.talktalk.domain.comment.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class Comment {

    private Long id;

    private Long postId;

    private Long userId;

    private String content;

    private LocalDateTime createdAt;

    public Comment(Long postId, Long userId, String content) {
        this.postId = postId;
        this.userId = userId;
        this.content = content;
        createdAt = LocalDateTime.now();
    }

    public void update(String content) {
        this.content = content;
    }
}
