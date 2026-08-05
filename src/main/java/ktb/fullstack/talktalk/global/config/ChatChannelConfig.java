package ktb.fullstack.talktalk.global.config;

import ktb.fullstack.talktalk.domain.chat.interceptor.ChatMembershipInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@RequiredArgsConstructor
public class ChatChannelConfig implements WebSocketMessageBrokerConfigurer {

    private final ChatMembershipInterceptor chatMembershipInterceptor;

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(chatMembershipInterceptor);
    }
}
