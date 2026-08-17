package ktb.fullstack.talktalk.domain.chat.fanout;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RoomMessageListener {

    private static final String ROOM_TOPIC_PREFIX = "/topic/chat/rooms/";

    private final SimpMessagingTemplate messagingTemplate;

    public void handle(RoomMessageEnvelope envelope) {

        messagingTemplate.convertAndSend(ROOM_TOPIC_PREFIX + envelope.roomId(), envelope.payload());
    }
}
