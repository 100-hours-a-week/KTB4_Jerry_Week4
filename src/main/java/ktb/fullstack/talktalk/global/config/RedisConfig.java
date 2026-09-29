package ktb.fullstack.talktalk.global.config;

import ktb.fullstack.talktalk.domain.chat.fanout.ChatFanoutChannels;
import ktb.fullstack.talktalk.domain.chat.fanout.RoomEventEnvelope;
import ktb.fullstack.talktalk.domain.chat.fanout.RoomEventListener;
import ktb.fullstack.talktalk.domain.chat.fanout.RoomMessageEnvelope;
import ktb.fullstack.talktalk.domain.chat.fanout.RoomMessageListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, RoomMessageEnvelope> roomMessageEnvelopeRedisTemplate(RedisConnectionFactory connectionFactory) {

        return jsonTemplate(connectionFactory, RoomMessageEnvelope.class);
    }

    @Bean
    public RedisTemplate<String, RoomEventEnvelope> roomEventEnvelopeRedisTemplate(RedisConnectionFactory connectionFactory) {

        return jsonTemplate(connectionFactory, RoomEventEnvelope.class);
    }

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory,
            RoomMessageListener roomMessageListener,
            RoomEventListener roomEventListener
    ) {

        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(
                adapter(roomMessageListener, RoomMessageEnvelope.class),
                ChannelTopic.of(ChatFanoutChannels.ROOM_MESSAGE));
        container.addMessageListener(
                adapter(roomEventListener, RoomEventEnvelope.class),
                ChannelTopic.of(ChatFanoutChannels.ROOM_EVENT));
        return container;
    }


    private <T> RedisTemplate<String, T> jsonTemplate(RedisConnectionFactory connectionFactory, Class<T> type) {

        RedisTemplate<String, T> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setValueSerializer(new JacksonJsonRedisSerializer<>(type));
        return template;
    }

    private MessageListenerAdapter adapter(Object listener, Class<?> type) {

        MessageListenerAdapter adapter = new MessageListenerAdapter(listener, "handle");
        adapter.setSerializer(new JacksonJsonRedisSerializer<>(type));
        adapter.afterPropertiesSet();
        return adapter;
    }
}
