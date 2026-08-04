package ktb.fullstack.talktalk.domain.chat.service;

import ktb.fullstack.talktalk.domain.chat.entity.ChatRoom;
import ktb.fullstack.talktalk.domain.chat.entity.ChatRoomMember;
import ktb.fullstack.talktalk.domain.chat.repository.ChatRoomMemberRepository;
import ktb.fullstack.talktalk.domain.chat.repository.ChatRoomRepository;
import ktb.fullstack.talktalk.domain.user.entity.User;
import ktb.fullstack.talktalk.domain.user.repository.UserRepository;
import ktb.fullstack.talktalk.global.exception.BusinessException;
import ktb.fullstack.talktalk.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ChatRoomCreator {

    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;

    @Transactional
    public ChatRoom create(String dmKey, Long requesterId, Long partnerId) {

        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_TOKEN));
        User partner = userRepository.findById(partnerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PARTNER_NOT_FOUND));
        ChatRoom room = chatRoomRepository.save(ChatRoom.dm(dmKey));

        chatRoomMemberRepository.save(new ChatRoomMember(room, requester));
        chatRoomMemberRepository.save(new ChatRoomMember(room, partner));
        return room;
    }
}
