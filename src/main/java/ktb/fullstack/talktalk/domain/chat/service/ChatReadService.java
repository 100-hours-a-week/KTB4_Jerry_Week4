package ktb.fullstack.talktalk.domain.chat.service;

import ktb.fullstack.talktalk.domain.chat.entity.ChatRoom;
import ktb.fullstack.talktalk.domain.chat.entity.ChatRoomMember;
import ktb.fullstack.talktalk.domain.chat.repository.ChatRoomRepository;
import ktb.fullstack.talktalk.domain.chat.repository.ChatRoomUpdater;
import ktb.fullstack.talktalk.domain.chat.repository.MessageRepository;
import ktb.fullstack.talktalk.global.exception.BusinessException;
import ktb.fullstack.talktalk.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatReadService {

    private final MessageRepository messageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomUpdater chatRoomUpdater;

    public void markRead(UUID roomId, Long userId, UUID lastReadMessageId) {

        chatRoomRepository.findByIdAndMembersUserId(roomId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_CHATROOM_MEMBER));

        if (lastReadMessageId != null
                && !messageRepository.existsByIdAndRoomId(lastReadMessageId, roomId)) {
            throw new BusinessException(ErrorCode.MESSAGE_NOT_FOUND);
        }

        chatRoomUpdater.advanceLastRead(roomId, userId, lastReadMessageId);
    }

    public long getUnreadCount(UUID roomId, Long userId) {

        ChatRoom room = chatRoomRepository.findByIdAndMembersUserId(roomId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_CHATROOM_MEMBER));

        UUID lastRead = room.memberOf(userId).map(ChatRoomMember::getLastReadMessageId).orElse(null);

        return lastRead == null
                ? messageRepository.countByRoomIdAndSenderIdNotAndDeletedAtIsNull(roomId, userId)
                : messageRepository.countByRoomIdAndSenderIdNotAndDeletedAtIsNullAndIdGreaterThan(roomId, userId, lastRead);
    }
}
