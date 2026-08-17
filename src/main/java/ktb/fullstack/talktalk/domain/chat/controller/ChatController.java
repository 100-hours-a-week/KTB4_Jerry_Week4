package ktb.fullstack.talktalk.domain.chat.controller;

import ktb.fullstack.talktalk.domain.chat.dto.request.ChatMessageSendRequestDto;
import ktb.fullstack.talktalk.domain.chat.dto.response.MessageErrorResponseDto;
import ktb.fullstack.talktalk.domain.chat.dto.response.MessageResponseDto;
import ktb.fullstack.talktalk.domain.chat.fanout.ChatFanoutPublisher;
import ktb.fullstack.talktalk.domain.chat.service.ChatRoomEventPublisher;
import ktb.fullstack.talktalk.domain.chat.service.MessageService;
import ktb.fullstack.talktalk.global.exception.BusinessException;
import ktb.fullstack.talktalk.global.resolver.LoginUserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final MessageService messageService;
    private final ChatRoomEventPublisher chatRoomEventPublisher;
    private final ChatFanoutPublisher chatFanoutPublisher;

    @MessageMapping("/chat/rooms/{roomId}")
    @SendToUser(destinations = "/queue/acks", broadcast = false)
    public MessageResponseDto handle(
            @DestinationVariable Long roomId,
            ChatMessageSendRequestDto request,
            Principal principal) {

        LoginUserInfo sender = (LoginUserInfo) ((Authentication) principal).getPrincipal();
        MessageResponseDto result = messageService.send(roomId, sender.userId(), request.content(), request.clientMessageId());
        chatFanoutPublisher.publishRoomMessage(roomId, result);
        chatRoomEventPublisher.publishNewMessage(roomId, sender.userId(), result.content(), result.createdAt());
        return result;
    }

    @MessageExceptionHandler(BusinessException.class)
    @SendToUser(destinations = "/queue/errors", broadcast = false)
    public MessageErrorResponseDto handleBusinessException(BusinessException e, @Payload ChatMessageSendRequestDto request) {

        return MessageErrorResponseDto.of(e.getErrorCode(), request.clientMessageId());
    }
}
