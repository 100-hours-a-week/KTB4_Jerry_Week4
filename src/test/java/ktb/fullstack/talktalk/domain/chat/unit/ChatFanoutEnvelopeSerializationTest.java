package ktb.fullstack.talktalk.domain.chat.unit;

import ktb.fullstack.talktalk.domain.chat.dto.response.ChatRoomEventDto;
import ktb.fullstack.talktalk.domain.chat.dto.response.ChatRoomEventType;
import ktb.fullstack.talktalk.domain.chat.dto.response.MessageResponseDto;
import ktb.fullstack.talktalk.domain.chat.fanout.RoomEventEnvelope;
import ktb.fullstack.talktalk.domain.chat.fanout.RoomMessageEnvelope;
import ktb.fullstack.talktalk.domain.user.dto.WriterDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Redis 팬아웃 봉투 직렬화")
public class ChatFanoutEnvelopeSerializationTest {

    private static final LocalDateTime SENT_AT = LocalDateTime.of(2026, 8, 17, 12, 12, 12, 400824000);
    private static final UUID ROOM_ID    = UUID.fromString("0198f3a2-7c40-7000-8a3f-1c2d3e4f5060");
    private static final UUID MESSAGE_ID = UUID.fromString("0198f3a2-7c40-7001-9b4e-2d3e4f506170");

    @Nested
    @DisplayName("채팅방 메시지 봉투")
    class RoomMessage {

        private final JacksonJsonRedisSerializer<RoomMessageEnvelope> serializer =
                new JacksonJsonRedisSerializer<>(RoomMessageEnvelope.class);

        @Test
        @DisplayName("LocalDateTime을 포함해 왕복해도 값이 보존된다")
        void 왕복해도_값_보존() {

            MessageResponseDto payload = new MessageResponseDto(MESSAGE_ID, ROOM_ID, 3L, "Hi", "cid-1", false, SENT_AT);
            RoomMessageEnvelope given = new RoomMessageEnvelope(ROOM_ID, payload);

            RoomMessageEnvelope actual = serializer.deserialize(serializer.serialize(given));

            assertThat(actual).isNotNull();
            assertThat(actual.roomId()).isEqualTo(ROOM_ID);
            assertThat(actual.payload().messageId()).isEqualTo(MESSAGE_ID);
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
            RoomEventEnvelope given = new RoomEventEnvelope(17L, new ChatRoomEventDto(ROOM_ID, partner, "Hi", SENT_AT, ChatRoomEventType.MESSAGE));

            RoomEventEnvelope actual = serializer.deserialize(serializer.serialize(given));

            assertThat(actual).isNotNull();
            assertThat(actual.targetUserId()).isEqualTo(17L);
            assertThat(actual.payload().roomId()).isEqualTo(ROOM_ID);
            assertThat(actual.payload().partner().getId()).isEqualTo(3L);
            assertThat(actual.payload().partner().getNickname()).isEqualTo("jerry");
            assertThat(actual.payload().partner().getProfileImageUrl()).isEqualTo("/images/a.png");
            assertThat(actual.payload().lastMessagePreview()).isEqualTo("Hi");
            assertThat(actual.payload().lastMessageAt()).isEqualTo(SENT_AT);
            assertThat(actual.payload().type()).isEqualTo(ChatRoomEventType.MESSAGE);
        }
    }
}
