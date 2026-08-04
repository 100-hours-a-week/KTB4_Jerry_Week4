package ktb.fullstack.talktalk.domain.chat.service;

import ktb.fullstack.talktalk.domain.chat.entity.ChatRoom;
import ktb.fullstack.talktalk.domain.chat.repository.ChatRoomRepository;
import ktb.fullstack.talktalk.global.common.response.CreateResponseDto;
import ktb.fullstack.talktalk.global.exception.BusinessException;
import ktb.fullstack.talktalk.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomCreator chatRoomCreator;

    public CreateResponseDto getOrCreateDm(Long requesterId, Long partnerId) {

        if (requesterId.equals(partnerId)) {
            throw new BusinessException(ErrorCode.CANNOT_CHAT_ALONE);
        }
        String dmKey = DmKey.of(requesterId, partnerId);
        ChatRoom room = chatRoomRepository.findByDmKey(dmKey)
                .orElseGet(() -> createOrRecover(dmKey, requesterId, partnerId));

        return new CreateResponseDto(room.getId());
    }

    private ChatRoom createOrRecover(String dmKey, Long requesterId, Long partnerId) {

        try {
            return chatRoomCreator.create(dmKey, requesterId, partnerId);

        } catch (DataIntegrityViolationException race) {
            return chatRoomRepository.findByDmKey(dmKey).orElseThrow(() -> race);
        }
    }
}
