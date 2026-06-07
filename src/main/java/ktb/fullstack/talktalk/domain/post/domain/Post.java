package ktb.fullstack.talktalk.domain.post.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class Post {

    private Long id;

    private Long userId;

    private String title;

    private String content;

    private int viewCount;

    private LocalDateTime createdAt;

    private String postImageUrl;

    public Post(Long userId, String title, String content, String postImageUrl) {
        this.userId = userId;
        this.title = title;
        this.content = content;
        this.postImageUrl = postImageUrl;
        this.viewCount = 0;
        this.createdAt = LocalDateTime.now();
    }

    public void increaseViewCount() {
        this.viewCount++;
    }

    public void update(String title, String content, String postImageUrl) {
        this.title = title;
        this.content = content;
        this.postImageUrl = postImageUrl;
    }
}
