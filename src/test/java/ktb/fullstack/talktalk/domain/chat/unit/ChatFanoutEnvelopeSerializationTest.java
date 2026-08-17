package ktb.fullstack.talktalk.domain.chat.unit;

import ktb.fullstack.talktalk.domain.chat.dto.response.ChatRoomEventDto;
import ktb.fullstack.talktalk.domain.chat.dto.response.MessageResponseDto;
import ktb.fullstack.talktalk.domain.chat.fanout.RoomEventEnvelope;
import ktb.fullstack.talktalk.domain.chat.fanout.RoomMessageEnvelope;
import ktb.fullstack.talktalk.domain.user.dto.WriterDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Redis 팬아웃 봉투 직렬화")
public class ChatFanoutEnvelopeSerializationTest {

    private static final LocalDateTime SENT_AT = LocalDateTime.of(2026, 8, 17, 12, 12, 12, 400824000);

    @Nested
    @DisplayName("채팅방 메시지 봉투")
    class RoomMessage {

        private final JacksonJsonRedisSerializer<RoomMessageEnvelope> serializer =
                new JacksonJsonRedisSerializer<>(RoomMessageEnvelope.class);

        @Test
        @DisplayName("LocalDateTime을 포함해 왕복해도 값이 보존된다")
        void 왕복해도_값_보존() {

            MessageResponseDto payload = new MessageResponseDto(7L, 42L, 3L, "Hi", "cid-1", false, SENT_AT);
            RoomMessageEnvelope given = new RoomMessageEnvelope(42L, payload);

            RoomMessageEnvelope actual = serializer.deserialize(serializer.serialize(given));

            assertThat(actual).isNotNull();
            assertThat(actual.roomId()).isEqualTo(42L);
            assertThat(actual.payload().messageId()).isEqualTo(7L);
            assertThat(actual.payload().content()).isEqualTo("Hi");
            assertThat(actual.payload().clientMessageId()).isEqualTo("cid-1");
            assertThat(actual.payload().deleted()).isFalse();
            assertThat(actual.payload().createdAt()).isEqualTo(SENT_AT);
        }
    }

    @Nested
    @DisplayName("채팅방 이벤트 봉투")
    class RoomEvent {

        private final JacksonJsonRedisSerializer<RoomEventEnvelope> serializer =
                new JacksonJsonRedisSerializer<>(RoomEventEnvelope.class);

        @Test
        @DisplayName("WriterDto를 포함해 왕복해도 값이 보존된다")
        void 왕복해도_값_보존() {

            WriterDto partner = new WriterDto(3L, "jerry", "/images/a.png");
            RoomEventEnvelope given = new RoomEventEnvelope(17L, new ChatRoomEventDto(42L, partner, "Hi", SENT_AT));

            RoomEventEnvelope actual = serializer.deserialize(serializer.serialize(given));

            assertThat(actual).isNotNull();
            assertThat(actual.targetUserId()).isEqualTo(17L);
            assertThat(actual.payload().roomId()).isEqualTo(42L);
            assertThat(actual.payload().partner().getId()).isEqualTo(3L);
            assertThat(actual.payload().partner().getNickname()).isEqualTo("jerry");
            assertThat(actual.payload().partner().getProfileImageUrl()).isEqualTo("/images/a.png");
            assertThat(actual.payload().lastMessagePreview()).isEqualTo("Hi");
            assertThat(actual.payload().lastMessageAt()).isEqualTo(SENT_AT);
        }
    }

}
