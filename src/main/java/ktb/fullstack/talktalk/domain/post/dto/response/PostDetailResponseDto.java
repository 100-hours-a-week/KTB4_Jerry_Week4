package ktb.fullstack.talktalk.domain.post.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import ktb.fullstack.talktalk.domain.comment.dto.response.CommentDto;
import ktb.fullstack.talktalk.domain.user.dto.WriterDto;
import ktb.fullstack.talktalk.global.common.response.CursorPageResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PostDetailResponseDto {

    private Long id;

    private String title;

    private String content;

    @JsonProperty("post_image_url")
    private String postImageUrl;

    @JsonProperty("like_count")
    private int likeCount;

    @JsonProperty("comment_count")
    private int commentCount;

    @JsonProperty("view_count")
    private int viewCount;

    @JsonProperty("created_at")
    private String createdAt;

    private WriterDto writer;

    private CursorPageResponse<CommentDto> comments;
}
