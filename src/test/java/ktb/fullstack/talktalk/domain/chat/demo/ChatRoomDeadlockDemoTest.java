package ktb.fullstack.talktalk.domain.chat.demo;

import ktb.fullstack.talktalk.domain.chat.entity.ChatRoom;
import ktb.fullstack.talktalk.domain.chat.entity.ChatRoomMember;
import ktb.fullstack.talktalk.domain.chat.repository.ChatRoomMemberRepository;
import ktb.fullstack.talktalk.domain.chat.repository.ChatRoomRepository;
import ktb.fullstack.talktalk.domain.chat.repository.MessageRepository;
import ktb.fullstack.talktalk.domain.chat.service.DmKey;
import ktb.fullstack.talktalk.domain.chat.service.MessageService;
import ktb.fullstack.talktalk.domain.user.entity.User;
import ktb.fullstack.talktalk.domain.user.repository.UserRepository;
import ktb.fullstack.talktalk.support.MySqlTestContainerConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.test.context.ActiveProfiles;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("mysqltest")
@Import(MySqlTestContainerConfig.class)
public class ChatRoomDeadlockDemoTest {

    private static final int THREADS = 16;
    private static final int ROUNDS = 20;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ChatRoomRepository chatRoomRepository;

    @Autowired
    ChatRoomMemberRepository chatRoomMemberRepository;

    @Autowired
    MessageRepository messageRepository;

    @Autowired
    MessageService messageService;

    Long roomId;
    List<Long> senderIds;

    @BeforeEach
    void setUp() {
        messageRepository.deleteAll();
        chatRoomMemberRepository.deleteAll();
        chatRoomRepository.deleteAll();
        userRepository.deleteAll();

        User a = userRepository.save(new User("a@a.a", "Pw123!", "a"));
        User b = userRepository.save(new User("b@b.b", "Pw123!", "b"));

        ChatRoom room = chatRoomRepository.save(ChatRoom.dm(DmKey.of(a.getId(), b.getId())));
        chatRoomMemberRepository.save(new ChatRoomMember(room, a));
        chatRoomMemberRepository.save(new ChatRoomMember(room, b));

        roomId = room.getId();
        senderIds = List.of(a.getId(), b.getId());
    }

    @Test
    @DisplayName("같은 채팅방에 동시 접속해도 데드락 없이 전부 저장된다")
    void 같은_채팅방_동시_요청() throws InterruptedException {

        AtomicInteger success = new AtomicInteger();
        AtomicInteger deadlock = new AtomicInteger();
        AtomicInteger lockTimeout = new AtomicInteger();
        Map<String, Integer> others = new ConcurrentHashMap<>();

        long startedAt = System.nanoTime();

        for (int round = 0; round < ROUNDS; round++) {
            ExecutorService pool = Executors.newFixedThreadPool(THREADS);
            CountDownLatch ready = new CountDownLatch(THREADS);
            CountDownLatch fire = new CountDownLatch(1);
            CountDownLatch done = new CountDownLatch(THREADS);

            for (int i = 0; i < THREADS; i++) {
                Long senderId = senderIds.get(i % senderIds.size());
                pool.submit(() -> {
                    ready.countDown();
                    try {
                        fire.await();
                        messageService.send(roomId, senderId, "hello", UUID.randomUUID().toString());
                        success.incrementAndGet();
                    } catch (PessimisticLockingFailureException e) {
                        switch (mysqlErrorCode(e)) {
                            case 1213 -> deadlock.incrementAndGet();
                            case 1205 -> lockTimeout.incrementAndGet();
                            default -> others.merge("lock:" + mysqlErrorCode(e), 1, Integer::sum);
                        }
                    } catch (Exception e) {
                        others.merge(e.getClass().getSimpleName(), 1, Integer::sum);
                    } finally {
                        done.countDown();
                    }
                });
            }

            ready.await(10, TimeUnit.SECONDS);
            fire.countDown();
            done.await(60, TimeUnit.SECONDS);
            pool.shutdownNow();
        }

        long elapsedMs = (System.nanoTime() - startedAt) / 1_000_000;
        int total = THREADS * ROUNDS;

        System.out.println("========== 같은 방 동시 전송 ==========");
        System.out.printf("total=%d success=%d deadlock=%d lockTimeout=%d%n",
                total, success.get(), deadlock.get(), lockTimeout.get());
        System.out.printf("기타 예외=%s%n", others.isEmpty() ? "없음" : others);
        System.out.printf("실패율=%.2f%%  소요=%dms  저장된 메시지=%d건%n",
                (total - success.get()) * 100.0 / total, elapsedMs, messageRepository.count());

        assertThat(deadlock.get()).isZero();
        assertThat(lockTimeout.get()).isZero();
        assertThat(others).isEmpty();
        assertThat(success.get()).isEqualTo(total);
        assertThat(messageRepository.count()).isEqualTo(total);
    }

    private static int mysqlErrorCode(Throwable e) {

        Throwable root = e;
        while (root.getCause() != null) {
            root = root.getCause();
        }
        return root instanceof SQLException se ? se.getErrorCode() : 0;
    }
}
