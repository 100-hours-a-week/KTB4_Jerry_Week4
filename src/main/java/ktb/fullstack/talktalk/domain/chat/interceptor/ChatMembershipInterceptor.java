package ktb.fullstack.talktalk.domain.chat.interceptor;

import ktb.fullstack.talktalk.domain.chat.repository.ChatRoomMemberRepository;
import ktb.fullstack.talktalk.global.exception.BusinessException;
import ktb.fullstack.talktalk.global.exception.ErrorCode;
import ktb.fullstack.talktalk.global.resolver.LoginUserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class ChatMembershipInterceptor implements ChannelInterceptor {

    private static final Pattern ROOM_DESTINATION = Pattern.compile("/chat/rooms/(\\d+)");

    private final ChatRoomMemberRepository chatRoomMemberRepository;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) return message;

        StompCommand cmd = accessor.getCommand();
        if (StompCommand.SUBSCRIBE.equals(cmd) || StompCommand.SEND.equals(cmd)) {
            Long roomId = extractRoomId(accessor.getDestination());

            if (roomId != null &&
                    !chatRoomMemberRepository.existsByRoomIdAndUserId(roomId, currentUserId(accessor))) {
                throw new BusinessException(ErrorCode.NOT_CHATROOM_MEMBER);
            }
        }
        return message;
    }

    private Long extractRoomId(String destination) {

        if (destination == null) return null;
        Matcher matcher = ROOM_DESTINATION.matcher(destination);
        return matcher.find() ? Long.parseLong(matcher.group(1)) : null;
    }

    private Long currentUserId(StompHeaderAccessor accessor) {

        Principal user = accessor.getUser();
        if (user == null) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        LoginUserInfo info = (LoginUserInfo) ((Authentication) user).getPrincipal();
        return info.userId();
    }
}
