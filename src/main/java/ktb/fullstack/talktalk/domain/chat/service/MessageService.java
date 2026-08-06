package ktb.fullstack.talktalk.domain.chat.service;

import ktb.fullstack.talktalk.domain.chat.dto.response.MessageResponseDto;
import ktb.fullstack.talktalk.domain.chat.entity.Message;
import ktb.fullstack.talktalk.domain.chat.repository.MessageRepository;
import ktb.fullstack.talktalk.global.exception.BusinessException;
import ktb.fullstack.talktalk.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final MessageWriter messageWriter;

    public MessageResponseDto send(Long roomId, Long senderId, String content, String clientMessageId) {

        if (content == null || content.isEmpty()) {
            throw new BusinessException(ErrorCode.EMPTY_MESSAGE);
        }

        if (clientMessageId == null || clientMessageId.isBlank()) {
            throw new BusinessException(ErrorCode.EMPTY_CLIENT_MESSAGE_ID);
        }

        Message message = messageRepository
                .findByRoomIdAndSenderIdAndClientMessageId(roomId, senderId, clientMessageId)
                .orElseGet(() -> saveOrRecover(roomId, senderId, content, clientMessageId));

        return MessageResponseDto.from(message);
    }

    private Message saveOrRecover(Long roomId, Long senderId, String content, String clientMessageId) {

        try {
            return messageWriter.write(roomId, senderId, content, clientMessageId);

        } catch (DataIntegrityViolationException race) {
            return messageRepository
                    .findByRoomIdAndSenderIdAndClientMessageId(roomId, senderId, clientMessageId)
                    .orElseThrow(() -> race);
        }
    }
}
