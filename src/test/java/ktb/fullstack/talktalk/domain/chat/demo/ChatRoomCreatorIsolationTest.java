package ktb.fullstack.talktalk.domain.chat.demo;

import ktb.fullstack.talktalk.domain.chat.entity.ChatRoom;
import ktb.fullstack.talktalk.domain.chat.repository.ChatRoomMemberRepository;
import ktb.fullstack.talktalk.domain.chat.repository.ChatRoomRepository;
import ktb.fullstack.talktalk.domain.chat.service.ChatRoomCreator;
import ktb.fullstack.talktalk.domain.user.entity.User;
import ktb.fullstack.talktalk.domain.user.repository.UserRepository;
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
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class ChatRoomCreatorIsolationTest {

    @TestConfiguration
    static class Beans {

        @Bean
        OuterTransactionalService outerTransactionalService(ChatRoomCreator creator, ChatRoomRepository repo) {

            return new OuterTransactionalService(creator, repo);
        }
    }

    @RequiredArgsConstructor
    static class OuterTransactionalService {

        private final ChatRoomCreator creator;
        private final ChatRoomRepository repo;

        @Transactional
        public ChatRoom getOrCreate(String dmKey, Long requesterId, Long partnerId) {

            try {
                return creator.create(dmKey, requesterId, partnerId);
            } catch (DataIntegrityViolationException e) {
                return repo.findByDmKey(dmKey).orElseThrow(() -> e);
            }
        }
    }

    @Autowired
    OuterTransactionalService outerTransactionalService;

    @Autowired
    ChatRoomRepository chatRoomRepository;

    @Autowired
    ChatRoomMemberRepository chatRoomMemberRepository;

    @Autowired
    UserRepository userRepository;

    Long meId;
    Long partnerId;

    @BeforeEach
    void setUp() {

        chatRoomMemberRepository.deleteAll();
        chatRoomRepository.deleteAll();
        userRepository.deleteAll();

        meId = userRepository.save(new User("me@a.a", "Pw123!", "me")).getId();
        partnerId = userRepository.save(new User("partner@a.a", "Pw123!", "partner")).getId();
    }

    @Test
    @DisplayName("호출하는 쪽에 트랜잭션이 있어도 생성 실패가 바깥을 오염시키지 않는다")
    void 바깥_트랜잭션에서도_격리된다() {

        String dmKey = meId + ":" + partnerId;
        chatRoomRepository.saveAndFlush(ChatRoom.dm(dmKey));

        ChatRoom recovered = outerTransactionalService.getOrCreate(dmKey, meId, partnerId);

        assertThat(recovered).isNotNull();
        assertThat(recovered.getDmKey()).isEqualTo(dmKey);
        assertThat(chatRoomRepository.count()).isEqualTo(1);
    }
}
