package ktb.fullstack.talktalk.domain.post.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "post_histories",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_post_history_version",
                columnNames = {"post_id", "version"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(nullable = false)
    private int version;

    @Column(nullable = false, length = 26)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @ElementCollection
    @OnDelete(action = OnDeleteAction.CASCADE)
    @CollectionTable(
            name = "post_history_images",
            joinColumns = @JoinColumn(name = "post_history_id")
    )
    @OrderColumn(name = "sort_order")
    @Column(name = "image_id")
    private List<Long> imageIds = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public PostHistory(Post post, int version, String title, String content, List<Long> imageIds) {
        this.post = post;
        this.version = version;
        this.title = title;
        this.content = content;

        if (imageIds != null) {
            this.imageIds.addAll(imageIds);
        }
    }
}
