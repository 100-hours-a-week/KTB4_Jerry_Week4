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
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ChatRoomEventPublisher {

    private final ChatFanoutPublisher chatFanoutPublisher;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final WriterResolver writerResolver;

    public void publishNewMessage(UUID roomId, Long senderId, WriterDto sender, String preview, LocalDateTime sendAt) {

        publish(roomId, senderId, sender, preview, sendAt, ChatRoomEventType.MESSAGE);
    }

    public void publishMessageDeleted(UUID roomId, Long actorId, String preview, LocalDateTime lastMessageAt) {

        publish(roomId, actorId, writerResolver.resolveWriter(actorId), preview, lastMessageAt, ChatRoomEventType.DELETED);
    }

    private void publish(UUID roomId, Long actorId, WriterDto actor, String preview, LocalDateTime lastMessageAt, ChatRoomEventType type) {

        List<RoomPartnerProjection> recipients = chatRoomMemberRepository.findPartners(List.of(roomId), actorId);
        for (RoomPartnerProjection recipient : recipients) {
            ChatRoomEventDto event = new ChatRoomEventDto(roomId, actor, preview, lastMessageAt, type);
            chatFanoutPublisher.publishRoomEvent(recipient.getPartnerId(), event);
        }
    }
}
