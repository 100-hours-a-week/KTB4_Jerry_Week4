package ktb.fullstack.talktalk.domain.post.entity;

import jakarta.persistence.*;
import ktb.fullstack.talktalk.domain.user.entity.User;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "post_views",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_post_view",
                columnNames = {"post_id", "user_id"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostView {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "last_viewed_at", nullable = false)
    private LocalDateTime lastViewedAt;

    public PostView(Post post, User user) {
        this.post = post;
        this.user = user;
        this.lastViewedAt = LocalDateTime.now();
    }

    public void updateLastViewedAt() {
        this.lastViewedAt = LocalDateTime.now();
    }
}
