package ktb.fullstack.talktalk.domain.post.entity;

import jakarta.persistence.*;
import ktb.fullstack.talktalk.domain.user.entity.User;
import ktb.fullstack.talktalk.global.common.entity.BaseTimeEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "posts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 26)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "view_count", nullable = false)
    private int viewCount;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "blinded_at")
    private LocalDateTime blindedAt;

    @Column(name = "is_edited", nullable = false)
    private boolean edited;

    public Post(User user, String title, String content) {
        this.user = user;
        this.title = title;
        this.content = content;
        this.viewCount = 0;
    }

    public void increaseViewCount() {
        this.viewCount++;
    }

    public void update(String title, String content) {
        this.title = title;
        this.content = content;
        this.edited = true;
    }

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    public void blind() {
        this.blindedAt = LocalDateTime.now();
    }
}
