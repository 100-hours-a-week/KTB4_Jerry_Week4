package ktb.fullstack.talktalk.domain.chat;

import jakarta.transaction.Transactional;
import ktb.fullstack.talktalk.domain.chat.entity.ChatRoom;
import ktb.fullstack.talktalk.domain.chat.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
public class TransactionPoisoningDemoTest {

    @TestConfiguration
    static class Beans {
        @Bean
        BadCreator badCreator(ChatRoomRepository r) {
            return new BadCreator(r);
        }

        @Bean
        InnerCreator innerCreator(ChatRoomRepository r) {
            return new InnerCreator(r);
        }

        @Bean
        GoodService goodService(InnerCreator i, ChatRoomRepository r) {
            return new GoodService(i, r);
        }
    }

    @RequiredArgsConstructor
    static class BadCreator {

        private final ChatRoomRepository repo;

        @Transactional
        public ChatRoom recover(String dmKey) {

            try {
                repo.saveAndFlush(ChatRoom.dm(dmKey));
                return null;

            } catch (DataIntegrityViolationException e) {
                return repo.findByDmKey(dmKey).orElseThrow(() -> e);
            }
        }
    }

    @RequiredArgsConstructor
    static class InnerCreator {

        private final ChatRoomRepository repo;

        @Transactional
        public ChatRoom create(String dmKey) {
            return repo.saveAndFlush(ChatRoom.dm(dmKey));
        }
    }

    @RequiredArgsConstructor
    static class GoodService {

        private final InnerCreator inner;
        private final ChatRoomRepository repo;

        public ChatRoom recover(String dmKey) {
            try {
                return inner.create(dmKey);

            } catch (DataIntegrityViolationException e) {
                return repo.findByDmKey(dmKey).orElseThrow(() -> e);
            }
        }
    }

    @Autowired
    BadCreator badCreator;

    @Autowired
    GoodService goodService;

    @Autowired
    ChatRoomRepository chatRoomRepository;

    @BeforeEach
    void clean() {
        chatRoomRepository.deleteAll();
    }

    @Test
    @DisplayName("BAD: @Transactional 안에서 위반 후 재조회 -> 롤백/세션 오염으로 실패")
    void bad_오염() {

        chatRoomRepository.saveAndFlush(ChatRoom.dm("1:2"));

        assertThatThrownBy(() -> badCreator.recover("1:2"))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("GOOD: 생성을 별도 @Transactional 빈으로 격리 -> 오염 없이 기존 방 복구")
    void good_복구() {

        chatRoomRepository.saveAndFlush(ChatRoom.dm("1:2"));
        ChatRoom recovered = goodService.recover("1:2");

        assertThat(recovered).isNotNull();
        assertThat(recovered.getDmKey()).isEqualTo("1:2");
    }
}
