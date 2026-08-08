package ktb.fullstack.talktalk.domain.chat.service;

import ktb.fullstack.talktalk.domain.chat.dto.response.ChatRoomEventDto;
import ktb.fullstack.talktalk.domain.chat.repository.ChatRoomMemberRepository;
import ktb.fullstack.talktalk.domain.chat.repository.RoomPartnerProjection;
import ktb.fullstack.talktalk.domain.user.dto.WriterDto;
import ktb.fullstack.talktalk.domain.user.service.WriterResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ChatRoomEventPublisher {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final WriterResolver writerResolver;

    public void publishNewMessage(Long roomId, Long senderId, String preview, LocalDateTime sendAt) {

        WriterDto sender = writerResolver.resolveWriter(senderId);
        List<RoomPartnerProjection> recipients = chatRoomMemberRepository.findPartners(List.of(roomId), senderId);
        for (RoomPartnerProjection recipient : recipients) {
            ChatRoomEventDto event = new ChatRoomEventDto(roomId, sender, preview, sendAt);
            messagingTemplate.convertAndSendToUser(
                    String.valueOf(recipient.getPartnerId()), "/queue/rooms", event);
        }
    }
}
