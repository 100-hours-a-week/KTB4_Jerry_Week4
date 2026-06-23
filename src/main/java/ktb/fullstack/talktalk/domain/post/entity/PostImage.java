package ktb.fullstack.talktalk.domain.post.entity;

import jakarta.persistence.*;
import ktb.fullstack.talktalk.domain.image.entity.Image;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(
        name = "post_images",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_post_image",
                columnNames = {"post_id", "image_id"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "image_id", nullable = false)
    private Image image;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    public PostImage(Post post, Image image, int sortOrder) {
        this.post = post;
        this.image = image;
        this.sortOrder = sortOrder;
    }
}
