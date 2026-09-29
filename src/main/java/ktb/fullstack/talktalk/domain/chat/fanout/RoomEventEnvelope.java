package ktb.fullstack.talktalk.domain.chat.fanout;

import ktb.fullstack.talktalk.domain.chat.dto.response.ChatRoomEventDto;

public record RoomEventEnvelope(Long targetUserId, ChatRoomEventDto payload) {
}
