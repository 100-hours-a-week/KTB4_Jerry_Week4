package ktb.fullstack.talktalk.domain.post.entity;

import jakarta.persistence.*;
import ktb.fullstack.talktalk.domain.user.entity.User;
import ktb.fullstack.talktalk.global.common.entity.BaseTimeEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "post_drafts",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_post_draft_user",
                columnNames = "user_id"
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostDraft extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(length = 26)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @ElementCollection
    @OnDelete(action = OnDeleteAction.CASCADE)
    @CollectionTable(
            name = "post_draft_images",
            joinColumns = @JoinColumn(name = "post_draft_id")
    )
    @OrderColumn(name = "sort_order")
    @Column(name = "image_id")
    private List<Long> imageIds = new ArrayList<>();

    public PostDraft(User user, String title, String content, List<Long> imageIds) {
        this.user = user;
        this.title = title;
        this.content = content;
        replaceImages(imageIds);
    }

    public void update(String title, String content, List<Long> imageIds) {
        this.title = title;
        this.content = content;
        replaceImages(imageIds);
    }

    private void replaceImages(List<Long> images) {
        this.imageIds.clear();
        if (images != null) {
            this.imageIds.addAll(images);
        }
    }
}
