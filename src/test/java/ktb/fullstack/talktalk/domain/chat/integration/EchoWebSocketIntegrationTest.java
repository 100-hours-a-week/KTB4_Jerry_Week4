package ktb.fullstack.talktalk.domain.chat.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;


import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
public class EchoWebSocketIntegrationTest {

    @LocalServerPort
    int port;

    @Test
    @DisplayName("보낸 텍스트가 그대로 되돌아온다")
    void 에코_왕복() throws Exception {

        BlockingQueue<String> received = new LinkedBlockingQueue<>();
        StandardWebSocketClient client = new StandardWebSocketClient();

        WebSocketSession session = client.execute(
                new TextWebSocketHandler() {
                    @Override
                    protected void handleTextMessage(WebSocketSession s, TextMessage message) {

                        received.offer(message.getPayload());
                    }
                },
                "ws://localhost:" + port + "/ws/echo"
        ).get(1, TimeUnit.SECONDS);

        session.sendMessage(new TextMessage("Hello"));

        String echo = received.poll(1, TimeUnit.SECONDS);
        assertThat(echo).isEqualTo("Hello");

        session.close();
    }
}
