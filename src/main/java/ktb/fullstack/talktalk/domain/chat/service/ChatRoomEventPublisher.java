package ktb.fullstack.talktalk.domain.chat.service;

import ktb.fullstack.talktalk.domain.chat.dto.response.ChatRoomEventDto;
import ktb.fullstack.talktalk.domain.chat.dto.response.ChatRoomEventType;
import ktb.fullstack.talktalk.domain.chat.fanout.ChatFanoutPublisher;
import ktb.fullstack.talktalk.domain.chat.repository.ChatRoomMemberRepository;
import ktb.fullstack.talktalk.domain.chat.repository.RoomPartnerProjection;
import ktb.fullstack.talktalk.domain.user.dto.WriterDto;
import ktb.fullstack.talktalk.domain.user.service.WriterResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ChatRoomEventPublisher {

    private final ChatFanoutPublisher chatFanoutPublisher;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final WriterResolver writerResolver;

    public void publishNewMessage(Long roomId, Long senderId, String preview, LocalDateTime sendAt) {

        publish(roomId, senderId, preview, sendAt, ChatRoomEventType.MESSAGE);
    }

    public void publishMessageDeleted(Long roomId, Long actorId, String preview, LocalDateTime lastMessageAt) {

        publish(roomId, actorId, preview, lastMessageAt, ChatRoomEventType.DELETED);
    }

    private void publish(Long roomId, Long actorId, String preview, LocalDateTime lastMessageAt, ChatRoomEventType type) {

        WriterDto sender = writerResolver.resolveWriter(actorId);
        List<RoomPartnerProjection> recipients = chatRoomMemberRepository.findPartners(List.of(roomId), actorId);
        for (RoomPartnerProjection recipient : recipients) {
            ChatRoomEventDto event = new ChatRoomEventDto(roomId, sender, preview, lastMessageAt, type);
            chatFanoutPublisher.publishRoomEvent(recipient.getPartnerId(), event);
        }
    }
}
