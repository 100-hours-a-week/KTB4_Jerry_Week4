package ktb.fullstack.talktalk.domain.chat.fanout;

import ktb.fullstack.talktalk.domain.chat.dto.response.MessageResponseDto;

public record RoomMessageEnvelope(Long roomId, MessageResponseDto payload) {
}
