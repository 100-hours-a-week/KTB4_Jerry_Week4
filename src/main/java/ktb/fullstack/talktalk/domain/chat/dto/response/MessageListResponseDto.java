package ktb.fullstack.talktalk.domain.chat.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MessageListResponseDto {

    private ChatCursorPageResponse<MessageResponseDto> messages;
}
