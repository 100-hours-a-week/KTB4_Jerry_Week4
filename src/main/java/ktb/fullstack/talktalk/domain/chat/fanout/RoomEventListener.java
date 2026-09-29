package ktb.fullstack.talktalk.domain.chat.fanout;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RoomEventListener {

    private static final String ROOM_QUEUE = "/queue/rooms";

    private final SimpMessagingTemplate messagingTemplate;

    public void handle(RoomEventEnvelope envelope) {

        messagingTemplate.convertAndSendToUser(
                String.valueOf(envelope.targetUserId()), ROOM_QUEUE, envelope.payload());
    }
}
