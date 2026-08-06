package ktb.fullstack.talktalk.domain.chat.dto.response;

import ktb.fullstack.talktalk.global.common.response.CursorPageResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MessageListResponseDto {

    private CursorPageResponse<MessageResponseDto> messages;
}
