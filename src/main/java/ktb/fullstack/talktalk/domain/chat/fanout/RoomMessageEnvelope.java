package ktb.fullstack.talktalk.domain.chat.fanout;

import ktb.fullstack.talktalk.domain.chat.dto.response.MessageResponseDto;

import java.util.UUID;

public record RoomMessageEnvelope(UUID roomId, MessageResponseDto payload) {
}
