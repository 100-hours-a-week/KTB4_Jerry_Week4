package ktb.fullstack.talktalk.domain.chat.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class ChatCursorPageResponse<T> {

    private List<T> items;

    @JsonProperty("next_cursor")
    private UUID nextCursor;
}
