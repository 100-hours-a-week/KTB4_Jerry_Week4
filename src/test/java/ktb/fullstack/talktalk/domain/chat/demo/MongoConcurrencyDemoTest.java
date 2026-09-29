package ktb.fullstack.talktalk.domain.chat.demo;

import ktb.fullstack.talktalk.domain.chat.entity.ChatRoom;
import ktb.fullstack.talktalk.domain.chat.entity.ChatRoomMember;
import ktb.fullstack.talktalk.domain.chat.entity.Message;
import ktb.fullstack.talktalk.domain.chat.repository.ChatRoomRepository;
import ktb.fullstack.talktalk.domain.chat.repository.MessageRepository;
import ktb.fullstack.talktalk.domain.chat.service.ChatReadService;
import ktb.fullstack.talktalk.domain.chat.service.DmKey;
import ktb.fullstack.talktalk.domain.chat.service.MessageService;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntConsumer;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("mongotest")
@Import({ MySqlTestContainerConfig.class, MongoTestContainerConfig.class })
public class MongoConcurrencyDemoTest {

    private static final int THREADS = 16;
    private static final int ROUNDS = 20;
    private static final int TOTAL = THREADS * ROUNDS;

    @Autowired UserRepository userRepository;
    @Autowired ChatRoomRepository chatRoomRepository;
    @Autowired MessageRepository messageRepository;
    @Autowired MessageService messageService;
    @Autowired ChatReadService chatReadService;

    UUID roomId;
    Long readerId;
    List<Long> senderIds;

    @BeforeEach
    void setUp() {
        messageRepository.deleteAll();
        chatRoomRepository.deleteAll();
        userRepository.deleteAll();

        User a = userRepository.save(new User("a@a.a", "Pw123!", "a"));
        User b = userRepository.save(new User("b@b.b", "Pw123!", "b"));

        ChatRoom room = ChatRoom.dm(DmKey.of(a.getId(), b.getId()));
        room.addMember(a.getId());
        room.addMember(b.getId());
        chatRoomRepository.save(room);

        roomId = room.getId();
        readerId = a.getId();
        senderIds = List.of(a.getId(), b.getId());
    }

    @Test
    @DisplayName("같은 방에 동시 전송해도 미리보기가 실제 최신 메시지로 수렴한다")
    void 동시_전송_미리보기_수렴() throws InterruptedException {

        AtomicInteger success = new AtomicInteger();
        Map<String, Integer> failures = new ConcurrentHashMap<>();

        long startedAt = System.nanoTime();

        for (int round = 0; round < ROUNDS; round++) {
            runConcurrently(i -> {
                Long senderId = senderIds.get(i % senderIds.size());
                messageService.send(roomId, senderId, "hello", UUID.randomUUID().toString());
                success.incrementAndGet();
            }, failures);
        }

        long elapsedMs = (System.nanoTime() - startedAt) / 1_000_000;

        UUID newestId = messageRepository.findByRoomIdOrderByIdDesc(roomId, PageRequest.of(0, 1))
                .getFirst().getId();
        ChatRoom room = chatRoomRepository.findById(roomId).orElseThrow();

        System.out.println("========== 동시 전송: 미리보기 수렴 ==========");
        System.out.printf("total=%d success=%d 실패=%s%n", TOTAL, success.get(),
                failures.isEmpty() ? "없음" : failures);
        System.out.printf("소요=%dms  처리량=%.0f msg/s  저장된 메시지=%d건%n",
                elapsedMs, success.get() * 1000.0 / elapsedMs, messageRepository.count());
        System.out.printf("실제 최신 메시지=%s%n방의 미리보기 =%s  일치=%s%n",
                newestId, room.getLastMessageId(), newestId.equals(room.getLastMessageId()));

        assertThat(failures).isEmpty();
        assertThat(success.get()).isEqualTo(TOTAL);
        assertThat(messageRepository.count()).isEqualTo(TOTAL);
        assertThat(room.getLastMessageId()).isEqualTo(newestId);
    }

    @Test
    @DisplayName("읽음 처리를 뒤섞어 동시 호출해도 읽음 포인터가 최댓값으로 수렴한다")
    void 동시_읽음_처리_수렴() throws InterruptedException {

        for (int i = 0; i < THREADS; i++) {
            messageService.send(roomId, senderIds.get(1), "hello", UUID.randomUUID().toString());
        }

        List<Message> messages = messageRepository.findByRoomIdOrderByIdDesc(roomId, PageRequest.of(0, THREADS));
        UUID newestId = messages.getFirst().getId();

        List<UUID> shuffled = new ArrayList<>(messages.stream().map(Message::getId).toList());
        Collections.shuffle(shuffled);

        Map<String, Integer> failures = new ConcurrentHashMap<>();
        runConcurrently(i -> chatReadService.markRead(roomId, readerId, shuffled.get(i)), failures);

        ChatRoom room = chatRoomRepository.findById(roomId).orElseThrow();
        UUID lastRead = room.memberOf(readerId).map(ChatRoomMember::getLastReadMessageId).orElseThrow();

        System.out.println("========== 동시 읽음 처리: 포인터 수렴 ==========");
        System.out.printf("동시 호출=%d  실패=%s%n", THREADS, failures.isEmpty() ? "없음" : failures);
        System.out.printf("실제 최신 메시지=%s%n읽음 포인터   =%s  일치=%s%n",
                newestId, lastRead, newestId.equals(lastRead));

        assertThat(failures).isEmpty();
        assertThat(lastRead).isEqualTo(newestId);
    }

    private void runConcurrently(IntConsumer task, Map<String, Integer> failures) throws InterruptedException {

        CountDownLatch ready = new CountDownLatch(THREADS);
        CountDownLatch fire = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(THREADS);

        try (ExecutorService pool = Executors.newFixedThreadPool(THREADS)) {
            for (int i = 0; i < THREADS; i++) {
                int index = i;
                pool.submit(() -> {
                    ready.countDown();
                    try {
                        fire.await();
                        task.accept(index);
                    } catch (Exception e) {
                        failures.merge(e.getClass().getSimpleName(), 1, Integer::sum);
                    } finally {
                        done.countDown();
                    }
                });
            }
            ready.await(10, TimeUnit.SECONDS);
            fire.countDown();
            assertThat(done.await(60, TimeUnit.SECONDS)).isTrue();
        }
    }
}
