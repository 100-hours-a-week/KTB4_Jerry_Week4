package ktb.fullstack.talktalk.domain.chat.integration;

import ktb.fullstack.talktalk.domain.auth.entity.Session;
import ktb.fullstack.talktalk.domain.auth.repository.SessionRepository;
import ktb.fullstack.talktalk.domain.chat.dto.request.ChatMessageSendRequestDto;
import ktb.fullstack.talktalk.domain.chat.dto.response.ChatRoomEventDto;
import ktb.fullstack.talktalk.domain.chat.dto.response.MessageErrorResponseDto;
import ktb.fullstack.talktalk.domain.chat.dto.response.MessageResponseDto;
import ktb.fullstack.talktalk.domain.chat.entity.ChatRoom;
import ktb.fullstack.talktalk.domain.chat.entity.ChatRoomMember;
import ktb.fullstack.talktalk.domain.chat.repository.ChatRoomMemberRepository;
import ktb.fullstack.talktalk.domain.chat.repository.ChatRoomRepository;
import ktb.fullstack.talktalk.domain.chat.repository.MessageRepository;
import ktb.fullstack.talktalk.domain.chat.service.DmKey;
import ktb.fullstack.talktalk.domain.user.entity.User;
import ktb.fullstack.talktalk.domain.user.repository.UserRepository;
import ktb.fullstack.talktalk.global.exception.ErrorCode;
import ktb.fullstack.talktalk.global.jwt.JwtProvider;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.JacksonJsonMessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
public class ChatStompIntegrationTest {

    @LocalServerPort
    int port;

    @Autowired
    UserRepository userRepository;
    @Autowired
    SessionRepository sessionRepository;
    @Autowired
    JwtProvider jwtProvider;
    @Autowired
    ChatRoomRepository chatRoomRepository;
    @Autowired
    ChatRoomMemberRepository chatRoomMemberRepository;
    @Autowired
    MessageRepository messageRepository;

    Long senderId;
    Long partnerId;
    Long roomId;
    String token;

    @BeforeEach
    void setUp() {

        messageRepository.deleteAll();
        chatRoomRepository.deleteAll();
        chatRoomMemberRepository.deleteAll();
        sessionRepository.deleteAll();
        userRepository.deleteAll();

        User one = userRepository.save(new User("sender@aaa.aaa", "Password123!", "one"));
        User other = userRepository.save(new User("other@aaa.aaa", "Password123!", "other"));
        Session session = sessionRepository.save(
                new Session(one, "refreshToken", LocalDateTime.now().plusDays(1)));

        ChatRoom room = chatRoomRepository.save(ChatRoom.dm(DmKey.of(one.getId(), other.getId())));

        chatRoomMemberRepository.save(new ChatRoomMember(room, one));
        chatRoomMemberRepository.save(new ChatRoomMember(room, other));

        senderId = one.getId();
        partnerId = other.getId();
        roomId = room.getId();
        token = jwtProvider.generateAccessToken(one.getId(), session.getId());
    }

    private WebSocketStompClient client() {

        WebSocketStompClient client = new WebSocketStompClient(new StandardWebSocketClient());
        client.setMessageConverter(new JacksonJsonMessageConverter());
        return client;
    }

    private StompSession connect() throws Exception {

        StompHeaders connectHeaders = new StompHeaders();
        connectHeaders.add("Authorization", "Bearer " + token);

        return client().connectAsync(
                "ws://localhost:" + port + "/ws",
                new WebSocketHttpHeaders(),
                connectHeaders,
                new StompSessionHandlerAdapter() {
                }
        ).get(1, TimeUnit.SECONDS);
    }

    private StompSession connectAs(String bearer) throws Exception {

        StompHeaders connectHeaders = new StompHeaders();
        connectHeaders.add("Authorization", "Bearer " + bearer);

        return client().connectAsync(
                "ws://localhost:" + port + "/ws",
                new WebSocketHttpHeaders(),
                connectHeaders,
                new StompSessionHandlerAdapter() {
                }
        ).get(1, TimeUnit.SECONDS);
    }

    private <T> BlockingQueue<T> subscribe(StompSession session, String destination, Class<T> payloadType) {

        BlockingQueue<T> received = new LinkedBlockingQueue<>();
        session.subscribe(destination, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return payloadType;
            }

            @Override
            public void handleFrame(StompHeaders headers, @Nullable Object payload) {
                received.offer(payloadType.cast(payload));
            }
        });
        return received;
    }


    @Test
    @DisplayName("채팅방으로 보낸 메시지가 저장되고, 그 채팅방 구독자에게 senderId와 함께 전달된다")
    void 채팅방_메시지_전송() throws Exception {

        StompSession session = connect();
        BlockingQueue<MessageResponseDto> received =
                subscribe(session, "/topic/chat/rooms/" + roomId, MessageResponseDto.class);

        String clientMessageId = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa";
        session.send("/app/chat/rooms/" + roomId,
                new ChatMessageSendRequestDto("Hi", clientMessageId));

        MessageResponseDto msg = received.poll(1, TimeUnit.SECONDS);
        assertThat(msg).isNotNull();
        assertThat(msg.roomId()).isEqualTo(roomId);
        assertThat(msg.senderId()).isEqualTo(senderId);
        assertThat(msg.content()).isEqualTo("Hi");
        assertThat(msg.messageId()).isNotNull();
        assertThat(msg.clientMessageId()).isEqualTo(clientMessageId);

        assertThat(messageRepository.count()).isEqualTo(1);

        session.disconnect();
    }

    @Test
    @DisplayName("같은 clientMessageId로 재전송해도 메시지는 한 번만 저장된다")
    void 재전송_멱등() throws Exception {

        StompSession session = connect();
        BlockingQueue<MessageResponseDto> received =
                subscribe(session, "/topic/chat/rooms/" + roomId, MessageResponseDto.class);

        String clientMessageId = "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb";
        session.send("/app/chat/rooms/" + roomId,
                new ChatMessageSendRequestDto("Hi", clientMessageId));
        session.send("/app/chat/rooms/" + roomId,
                new ChatMessageSendRequestDto("Hi", clientMessageId));

        MessageResponseDto first = received.poll(1, TimeUnit.SECONDS);
        MessageResponseDto second = received.poll(1, TimeUnit.SECONDS);

        assertThat(first).isNotNull();
        assertThat(second).isNotNull();
        assertThat(second.messageId()).isEqualTo(first.messageId());

        assertThat(messageRepository.count()).isEqualTo(1);

        session.disconnect();
    }

    @Test
    @DisplayName("메시지를 보내면 발신자에게 clientMessageId와 messageId를 담은 ACK가 도착한다")
    void ACK_수신() throws Exception {

        StompSession session = connect();
        BlockingQueue<MessageResponseDto> acks =
                subscribe(session, "/user/queue/acks", MessageResponseDto.class);

        String clientMessageId = "cccccccc-cccc-cccc-cccc-cccccccccccc";
        session.send("/app/chat/rooms/" + roomId,
                new ChatMessageSendRequestDto("Hi", clientMessageId));

        MessageResponseDto ack = acks.poll(1, TimeUnit.SECONDS);
        assertThat(ack).isNotNull();
        assertThat(ack.clientMessageId()).isEqualTo(clientMessageId);
        assertThat(ack.messageId()).isNotNull();
        assertThat(ack.roomId()).isEqualTo(roomId);

        session.disconnect();
    }

    @Test
    @DisplayName("전송이 실패하면 발신자에게 에러 코드와 clientMessageId가 도착한다")
    void 전송_실패_에러_수신() throws Exception {

        StompSession session = connect();
        BlockingQueue<MessageErrorResponseDto> errors =
                subscribe(session, "/user/queue/errors", MessageErrorResponseDto.class);

        String clientMessageId = "dddddddd-dddd-dddd-dddd-dddddddddddd";
        session.send("/app/chat/rooms/" + roomId, new ChatMessageSendRequestDto("", clientMessageId));

        MessageErrorResponseDto error = errors.poll(1, TimeUnit.SECONDS);
        assertThat(error).isNotNull();
        assertThat(error.code()).isEqualTo(ErrorCode.EMPTY_MESSAGE.name());
        assertThat(error.clientMessageId()).isEqualTo(clientMessageId);
        assertThat(messageRepository.count()).isZero();

        session.disconnect();
    }

    @Test
    @DisplayName("인증 실패로 STOMP 프레임이 거부되면 ERROR 프레임 message 헤더에 invalid_token가 담긴다")
    void 인증_실패로_거부되면_ERROR_프레임_헤더에_코드가_담긴다() throws Exception {

        BlockingQueue<StompHeaders> errorFrames = new LinkedBlockingQueue<>();
        StompHeaders connectHeaders = new StompHeaders();
        connectHeaders.add("Authorization", "Bearer invalid-token");

        client().connectAsync(
                "ws://localhost:" + port + "/ws",
                new WebSocketHttpHeaders(),
                connectHeaders,
                new StompSessionHandlerAdapter() {
                    @Override
                    public void handleFrame(StompHeaders headers, @Nullable Object payload) {
                        errorFrames.offer(headers);
                    }
                });

        StompHeaders errorHeaders = errorFrames.poll(2, TimeUnit.SECONDS);
        assertThat(errorHeaders).isNotNull();
        assertThat(errorHeaders.getFirst("message")).isEqualTo(ErrorCode.INVALID_TOKEN.getMessage());
    }

    @Test
    @DisplayName("메시지를 보내면 상대방의 /user/queue/rooms로 채팅방 목록 갱신 이벤트가 도착한다")
    void 채팅방_목록_실시간_이벤트_수신() throws Exception {

        User partner = userRepository.findById(partnerId).orElseThrow();
        Session partnerSession = sessionRepository.save(
                new Session(partner, "refreshToken2", LocalDateTime.now().plusDays(1)));
        String partnerToken = jwtProvider.generateAccessToken(partner.getId(), partnerSession.getId());

        StompSession partnerStomp = connectAs(partnerToken);
        BlockingQueue<ChatRoomEventDto> events =
                subscribe(partnerStomp, "/user/queue/rooms", ChatRoomEventDto.class);

        StompSession senderStomp = connect();
        senderStomp.send("/app/chat/rooms/" + roomId,
                new ChatMessageSendRequestDto("Hi", "eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee"));

        ChatRoomEventDto event = events.poll(2, TimeUnit.SECONDS);
        assertThat(event).isNotNull();
        assertThat(event.roomId()).isEqualTo(roomId);
        assertThat(event.partner().getId()).isEqualTo(senderId);
        assertThat(event.lastMessagePreview()).isEqualTo("Hi");

        partnerStomp.disconnect();
        senderStomp.disconnect();
    }
}
