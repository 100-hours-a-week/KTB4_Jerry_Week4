package ktb.fullstack.talktalk.global.common.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class CursorPageResponse<T> {

    private List<T> items;

    @JsonProperty("next_cursor")
    private Long nextCursor;
}
