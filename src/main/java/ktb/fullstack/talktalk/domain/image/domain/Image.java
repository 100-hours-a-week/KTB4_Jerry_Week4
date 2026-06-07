package ktb.fullstack.talktalk.domain.image.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class Image {

    private Long id;

    private String fileName;

    private LocalDateTime createdAt;

    public Image(String storedFileName) {
        this.fileName = storedFileName;
        this.createdAt = LocalDateTime.now();
    }
}
