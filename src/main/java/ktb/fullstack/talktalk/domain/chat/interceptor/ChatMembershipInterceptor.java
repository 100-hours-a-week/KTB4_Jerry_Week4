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
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class ChatMembershipInterceptor implements ChannelInterceptor {

    private static final Pattern ROOM_TOPIC = Pattern.compile("/topic/chat/rooms/(\\d{1,18})");
    private static final Pattern ROOM_APP = Pattern.compile("/app/chat/rooms/(\\d{1,18})");
    private static final Set<String> PERSONAL_QUEUES = Set.of("/user/queue/acks", "/user/queue/errors", "/user/queue/rooms");

    private final ChatRoomMemberRepository chatRoomMemberRepository;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) return message;

        StompCommand cmd = accessor.getCommand();
        if (StompCommand.SUBSCRIBE.equals(cmd)) {
            authorizeSubscribe(destination(accessor), accessor);
        } else if (StompCommand.SEND.equals(cmd)) {
            authorizeSend(destination(accessor), accessor);
        }

        return message;
    }

    private void authorizeSubscribe(String destination, StompHeaderAccessor accessor) {

        if (PERSONAL_QUEUES.contains(destination)) return;

        verifyMember(roomId(ROOM_TOPIC, destination), currentUserId(accessor));
    }

    private void authorizeSend(String destination, StompHeaderAccessor accessor) {

        verifyMember(roomId(ROOM_APP, destination), currentUserId(accessor));
    }

    private String destination(StompHeaderAccessor accessor) {

        String destination = accessor.getDestination();
        if (destination == null) {
            throw new BusinessException(ErrorCode.INVALID_DESTINATION);
        }
        return destination;
    }

    private void verifyMember(Long roomId, Long userId) {

        if (!chatRoomMemberRepository.existsByRoomIdAndUserId(roomId, userId)) {
            throw new BusinessException(ErrorCode.NOT_CHATROOM_MEMBER);
        }
    }

    private Long roomId(Pattern pattern, String destination) {

        Matcher matcher = pattern.matcher(destination);
        if (!matcher.matches()) {
            throw new BusinessException(ErrorCode.INVALID_DESTINATION);
        }
        return Long.parseLong(matcher.group(1));
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
