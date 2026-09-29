package ktb.fullstack.talktalk.domain.chat.service;

import ktb.fullstack.talktalk.domain.chat.entity.ChatRoom;
import ktb.fullstack.talktalk.domain.chat.repository.ChatRoomRepository;
import ktb.fullstack.talktalk.domain.user.repository.UserRepository;
import ktb.fullstack.talktalk.global.exception.BusinessException;
import ktb.fullstack.talktalk.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatRoomCreator {

    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;

    public ChatRoom create(String dmKey, Long requesterId, Long partnerId) {

        userRepository.findById(requesterId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_TOKEN));
        userRepository.findById(partnerId)
                .filter(user -> user.getDeletedAt() == null)
                .orElseThrow(() -> new BusinessException(ErrorCode.PARTNER_NOT_FOUND));

        ChatRoom room = ChatRoom.dm(dmKey);
        room.addMember(requesterId);
        room.addMember(partnerId);
        return chatRoomRepository.save(room);
    }
}
