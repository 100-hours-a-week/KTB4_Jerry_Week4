package ktb.fullstack.talktalk.domain.post.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import ktb.fullstack.talktalk.domain.user.dto.WriterDto;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PostSummaryDto {

    private Long id;

    private String title;

    @JsonProperty("like_count")
    private int likeCount;

    @JsonProperty("comment_count")
    private int commentCount;

    @JsonProperty("view_count")
    private int viewCount;

    @JsonProperty("created_at")
    private String createdAt;

    private WriterDto writer;
}
