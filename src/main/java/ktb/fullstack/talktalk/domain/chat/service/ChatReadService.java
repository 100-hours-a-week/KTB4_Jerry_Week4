package ktb.fullstack.talktalk.domain.chat.service;

import ktb.fullstack.talktalk.domain.chat.entity.ChatRoomMember;
import ktb.fullstack.talktalk.domain.chat.repository.ChatRoomMemberRepository;
import ktb.fullstack.talktalk.domain.chat.repository.MessageRepository;
import ktb.fullstack.talktalk.global.exception.BusinessException;
import ktb.fullstack.talktalk.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatReadService {

    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final MessageRepository messageRepository;

    @Transactional
    public void markRead(UUID roomId, Long userId, UUID lastReadMessageId) {

        ChatRoomMember member = chatRoomMemberRepository.findByRoomIdAndUserId(roomId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_CHATROOM_MEMBER));

        if (lastReadMessageId != null
                && !messageRepository.existsByIdAndRoomId(lastReadMessageId, roomId)) {
            throw new BusinessException(ErrorCode.MESSAGE_NOT_FOUND);
        }

        member.updateLastRead(lastReadMessageId);
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(UUID roomId, Long userId) {

        ChatRoomMember member = chatRoomMemberRepository.findByRoomIdAndUserId(roomId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_CHATROOM_MEMBER));
        UUID lastRead = member.getLastReadMessageId();
        return lastRead == null
                ? messageRepository.countByRoomIdAndSenderIdNotAndDeletedAtIsNull(roomId, userId)
                : messageRepository.countByRoomIdAndSenderIdNotAndDeletedAtIsNullAndIdGreaterThan(roomId, userId, lastRead);
    }
}
