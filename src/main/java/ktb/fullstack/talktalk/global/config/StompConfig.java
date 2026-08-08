package ktb.fullstack.talktalk.global.config;

import ktb.fullstack.talktalk.global.handler.StompErrorHandler;
import ktb.fullstack.talktalk.global.interceptor.StompAuthChannelInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class StompConfig implements WebSocketMessageBrokerConfigurer {

    private final StompAuthChannelInterceptor authChannelInterceptor;
    private final StompErrorHandler stompErrorHandler;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {

        registry.setErrorHandler(stompErrorHandler);
        registry.addEndpoint("/ws")
                .setAllowedOrigins("http://localhost:5173", "http://127.0.0.1:5173", "https://ktb-talktalk.site");
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {

        registry.setApplicationDestinationPrefixes("/app");
        registry.enableSimpleBroker("/topic", "/queue");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {

        registration.interceptors(authChannelInterceptor);
    }
}
