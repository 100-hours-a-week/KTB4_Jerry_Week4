package ktb.fullstack.talktalk.domain.user.entity;

import jakarta.persistence.*;
import ktb.fullstack.talktalk.domain.image.entity.Image;
import ktb.fullstack.talktalk.global.common.entity.BaseTimeEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "profile_images")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProfileImage extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "image_id", nullable = false)
    private Image image;

    @Column(name = "is_current", nullable = false)
    private boolean current;

    public ProfileImage(User user, Image image, boolean current) {
        this.user = user;
        this.image = image;
        this.current = current;
    }

    public void markCurrent() {
        this.current = true;
    }

    public void unmarkCurrent() {
        this.current = false;
    }
}
