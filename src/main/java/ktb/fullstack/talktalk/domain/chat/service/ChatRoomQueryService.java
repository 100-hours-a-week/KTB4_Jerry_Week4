package ktb.fullstack.talktalk.domain.chat.service;

import ktb.fullstack.talktalk.domain.chat.dto.response.ChatCursorPageResponse;
import ktb.fullstack.talktalk.domain.chat.dto.response.ChatRoomDetailResponseDto;
import ktb.fullstack.talktalk.domain.chat.dto.response.ChatRoomListResponseDto;
import ktb.fullstack.talktalk.domain.chat.dto.response.ChatRoomSummaryDto;
import ktb.fullstack.talktalk.domain.chat.entity.ChatRoom;
import ktb.fullstack.talktalk.domain.chat.repository.ChatRoomMemberRepository;
import ktb.fullstack.talktalk.domain.chat.repository.ChatRoomRepository;
import ktb.fullstack.talktalk.domain.chat.repository.RoomPartnerProjection;
import ktb.fullstack.talktalk.domain.chat.repository.MessageUnreadCounter;
import ktb.fullstack.talktalk.domain.chat.entity.ChatRoomMember;
import ktb.fullstack.talktalk.domain.user.dto.WriterDto;
import ktb.fullstack.talktalk.domain.user.service.WriterResolver;
import ktb.fullstack.talktalk.global.exception.BusinessException;
import ktb.fullstack.talktalk.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatRoomQueryService {

    private static final int PAGE_SIZE = 20;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final WriterResolver writerResolver;
    private final MessageUnreadCounter messageUnreadCounter;

    @Transactional(readOnly = true)
    public ChatRoomListResponseDto getMyRooms(Long userId, UUID cursor) {

        List<ChatRoom> rooms = chatRoomRepository.findRoomsByMemberAndCursor(
                userId, cursor, PageRequest.of(0, PAGE_SIZE + 1));

        boolean hasNext = rooms.size() > PAGE_SIZE;
        List<ChatRoom> pageContent = hasNext ? rooms.subList(0, PAGE_SIZE) : rooms;
        UUID nextCursor = hasNext ? rooms.getLast().getLastMessageId() : null;

        if (pageContent.isEmpty()) {
            return new ChatRoomListResponseDto(new ChatCursorPageResponse<>(List.of(), nextCursor));
        }

        List<UUID> roomIds = pageContent.stream().map(ChatRoom::getId).toList();
        Map<UUID, Long> partnerIdByRoom = chatRoomMemberRepository.findPartners(roomIds, userId).stream()
                .collect(Collectors.toMap(RoomPartnerProjection::getRoomId, RoomPartnerProjection::getPartnerId));

        List<Long> partnerIds = partnerIdByRoom.values().stream().distinct().toList();
        Map<Long, WriterDto> partners = writerResolver.resolveWriters(partnerIds);

        Map<UUID, UUID> lastReadByRoom = new HashMap<>();
        for (UUID roomId : roomIds) lastReadByRoom.put(roomId, null);
        for (ChatRoomMember member : chatRoomMemberRepository.findByRoomIdInAndUserId(roomIds, userId)) {
            lastReadByRoom.put(member.getRoom().getId(), member.getLastReadMessageId());
        }
        Map<UUID, Long> unreadByRoom = messageUnreadCounter.countByRooms(lastReadByRoom, userId);

        List<ChatRoomSummaryDto> items = pageContent.stream()
                .map(room -> new ChatRoomSummaryDto(
                        room.getId(),
                        partners.get(partnerIdByRoom.get(room.getId())),
                        room.getLastMessagePreview(),
                        room.getLastMessageAt(),
                        unreadByRoom.getOrDefault(room.getId(), 0L)))
                .toList();

        return new ChatRoomListResponseDto(new ChatCursorPageResponse<>(items, nextCursor));
    }

    @Transactional(readOnly = true)
    public ChatRoomDetailResponseDto getRoom(UUID roomId, Long userId) {

        if (!chatRoomRepository.existsById(roomId)) {
            throw new BusinessException(ErrorCode.CHATROOM_NOT_FOUND);
        }

        if (!chatRoomMemberRepository.existsByRoomIdAndUserId(roomId, userId)) {
            throw new BusinessException(ErrorCode.NOT_CHATROOM_MEMBER);
        }

        Long partnerId = chatRoomMemberRepository.findPartners(List.of(roomId), userId).stream()
                .map(RoomPartnerProjection::getPartnerId)
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.PARTNER_NOT_FOUND));

        WriterDto partner = writerResolver.resolveWriter(partnerId);
        return new ChatRoomDetailResponseDto(roomId, partner);
    }
}
