package ktb.fullstack.talktalk.domain.post.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import ktb.fullstack.talktalk.domain.comment.dto.response.CommentDto;
import ktb.fullstack.talktalk.domain.user.dto.WriterDto;
import ktb.fullstack.talktalk.global.common.response.CursorPageResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class PostDetailResponseDto {

    private Long id;

    private String title;

    private String content;

    @JsonProperty("post_images")
    private List<PostImageDto> postImages;

    @JsonProperty("is_liked")
    private boolean liked;

    @JsonProperty("like_count")
    private int likeCount;

    @JsonProperty("comment_count")
    private int commentCount;

    @JsonProperty("view_count")
    private int viewCount;

    @JsonProperty("created_at")
    private String createdAt;

    private WriterDto writer;

    @JsonProperty("is_edited")
    private boolean edited;

    @JsonProperty("is_blinded")
    private boolean blinded;

    private CursorPageResponse<CommentDto> comments;
}
