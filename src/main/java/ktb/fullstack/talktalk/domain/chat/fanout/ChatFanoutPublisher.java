package ktb.fullstack.talktalk.domain.chat.fanout;

import ktb.fullstack.talktalk.domain.chat.dto.response.ChatRoomEventDto;
import ktb.fullstack.talktalk.domain.chat.dto.response.MessageResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatFanoutPublisher {

    private final RedisTemplate<String, RoomMessageEnvelope> roomMessageRedisTemplate;
    private final RedisTemplate<String, RoomEventEnvelope> roomEventRedisTemplate;

    public void publishRoomMessage(Long roomId, MessageResponseDto payload) {

        roomMessageRedisTemplate.convertAndSend(
                ChatFanoutChannels.ROOM_MESSAGE, new RoomMessageEnvelope(roomId, payload));
    }

    public void publishRoomEvent(Long targetUserId, ChatRoomEventDto payload) {

        roomEventRedisTemplate.convertAndSend(
                ChatFanoutChannels.ROOM_EVENT, new RoomEventEnvelope(targetUserId, payload));
    }
}
