package ktb.fullstack.talktalk.domain.chat.integration;

import ktb.fullstack.talktalk.domain.chat.dto.response.ChatRoomCreateResponseDto;
import ktb.fullstack.talktalk.domain.chat.repository.ChatRoomRepository;
import ktb.fullstack.talktalk.domain.chat.repository.MessageRepository;
import ktb.fullstack.talktalk.domain.chat.service.ChatRoomCreator;
import ktb.fullstack.talktalk.domain.chat.service.ChatRoomService;
import ktb.fullstack.talktalk.domain.chat.service.DmKey;
import ktb.fullstack.talktalk.domain.user.entity.User;
import ktb.fullstack.talktalk.domain.user.repository.UserRepository;
import ktb.fullstack.talktalk.support.MongoTestContainerConfig;
import ktb.fullstack.talktalk.support.MySqlTestContainerConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static java.util.Collections.synchronizedList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("mongotest")
@Import({ MySqlTestContainerConfig.class, MongoTestContainerConfig.class })
class ChatRoomDmKeyRaceIntegrationTest {

    private static final int CONCURRENCY = 8;

    @Autowired UserRepository userRepository;
    @Autowired ChatRoomRepository chatRoomRepository;
    @Autowired MessageRepository messageRepository;
    @Autowired ChatRoomService chatRoomService;
    @Autowired ChatRoomCreator chatRoomCreator;

    Long meId;
    Long partnerId;

    @BeforeEach
    void setUp() {
        messageRepository.deleteAll();
        chatRoomRepository.deleteAll();
        userRepository.deleteAll();

        meId = userRepository.save(new User("me@a.a", "pw", "me")).getId();
        partnerId = userRepository.save(new User("alice@a.a", "pw", "alice")).getId();
    }

    @Test
    @DisplayName("같은 dmKey로 두 번 생성하면 DataIntegrityViolationException이 발생한다")
    void 중복_dmKey_거부() {

        String dmKey = DmKey.of(meId, partnerId);
        chatRoomCreator.create(dmKey, meId, partnerId);

        assertThatThrownBy(() -> chatRoomCreator.create(dmKey, meId, partnerId))
                .isInstanceOf(DataIntegrityViolationException.class);

        assertThat(chatRoomRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("동시에 같은 상대에게 DM을 요청해도 방은 하나만 생기고 모두 같은 방을 받는다")
    void 동시_DM_생성_경합() throws InterruptedException {

        CountDownLatch ready = new CountDownLatch(CONCURRENCY);
        CountDownLatch fire = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(CONCURRENCY);

        List<UUID> roomIds = synchronizedList(new ArrayList<>());
        List<Throwable> failures = synchronizedList(new ArrayList<>());

        try (ExecutorService pool = Executors.newFixedThreadPool(CONCURRENCY)) {
            for (int i = 0; i < CONCURRENCY; i++) {
                boolean reversed = i % 2 == 0;
                pool.submit(() -> {
                    ready.countDown();
                    try {
                        fire.await();
                        ChatRoomCreateResponseDto response = reversed
                                ? chatRoomService.getOrCreateDm(meId, partnerId)
                                : chatRoomService.getOrCreateDm(partnerId, meId);
                        roomIds.add(response.id());
                    } catch (Throwable t) {
                        failures.add(t);
                    } finally {
                        done.countDown();
                    }
                });
            }
            ready.await();
            fire.countDown();
            assertThat(done.await(30, TimeUnit.SECONDS)).isTrue();
        }

        assertThat(failures).isEmpty();
        assertThat(roomIds).hasSize(CONCURRENCY);
        assertThat(roomIds).containsOnly(roomIds.getFirst());
        assertThat(chatRoomRepository.count()).isEqualTo(1);
    }
}
