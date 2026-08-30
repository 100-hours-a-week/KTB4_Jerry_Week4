package ktb.fullstack.talktalk.domain.chat.service;

import ktb.fullstack.talktalk.domain.chat.dto.response.ChatCursorPageResponse;
import ktb.fullstack.talktalk.domain.chat.dto.response.ChatRoomDetailResponseDto;
import ktb.fullstack.talktalk.domain.chat.dto.response.ChatRoomListResponseDto;
import ktb.fullstack.talktalk.domain.chat.dto.response.ChatRoomSummaryDto;
import ktb.fullstack.talktalk.domain.chat.entity.ChatRoom;
import ktb.fullstack.talktalk.domain.chat.repository.ChatRoomRepository;
import ktb.fullstack.talktalk.domain.chat.repository.MessageUnreadCounter;
import ktb.fullstack.talktalk.domain.chat.entity.ChatRoomMember;
import ktb.fullstack.talktalk.domain.user.dto.WriterDto;
import ktb.fullstack.talktalk.domain.user.service.WriterResolver;
import ktb.fullstack.talktalk.global.exception.BusinessException;
import ktb.fullstack.talktalk.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatRoomQueryService {

    private static final int PAGE_SIZE = 20;
    private final ChatRoomRepository chatRoomRepository;
    private final WriterResolver writerResolver;
    private final MessageUnreadCounter messageUnreadCounter;

    public ChatRoomListResponseDto getMyRooms(Long userId, UUID cursor) {

        PageRequest page = PageRequest.of(0, PAGE_SIZE + 1, Sort.by(Sort.Direction.DESC, "lastMessage.id"));
        List<ChatRoom> rooms = cursor == null
                ? chatRoomRepository.findRoomsByMember(userId, page)
                : chatRoomRepository.findRoomsByMemberAndCursor(userId, cursor, page);

        boolean hasNext = rooms.size() > PAGE_SIZE;
        List<ChatRoom> pageContent = hasNext ? rooms.subList(0, PAGE_SIZE) : rooms;
        UUID nextCursor = hasNext ? rooms.getLast().getLastMessageId() : null;

        if (pageContent.isEmpty()) {
            return new ChatRoomListResponseDto(new ChatCursorPageResponse<>(List.of(), nextCursor));
        }

        Map<UUID, Long> partnerIdByRoom = new HashMap<>();
        Map<UUID, UUID> lastReadByRoom = new HashMap<>();

        for (ChatRoom room: pageContent) {
            room.partnerOf(userId).ifPresent(partnerId -> partnerIdByRoom.put(room.getId(), partnerId));
            lastReadByRoom.put(room.getId(), room.memberOf(userId).map(ChatRoomMember::getLastReadMessageId).orElse(null));
        }

        List<Long> partnerIds = partnerIdByRoom.values().stream().distinct().toList();
        Map<Long, WriterDto> partners = writerResolver.resolveWriters(partnerIds);
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

    public ChatRoomDetailResponseDto getRoom(UUID roomId, Long userId) {

        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHATROOM_NOT_FOUND));

        if (room.memberOf(userId).isEmpty()) {
            throw new BusinessException(ErrorCode.NOT_CHATROOM_MEMBER);
        }

        Long partnerId = room.partnerOf(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PARTNER_NOT_FOUND));

        WriterDto partner = writerResolver.resolveWriter(partnerId);
        return new ChatRoomDetailResponseDto(roomId, partner);
    }
}
