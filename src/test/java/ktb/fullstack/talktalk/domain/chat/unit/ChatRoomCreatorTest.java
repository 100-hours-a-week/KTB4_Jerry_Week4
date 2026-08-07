package ktb.fullstack.talktalk.domain.chat.unit;

import ktb.fullstack.talktalk.domain.chat.entity.ChatRoom;
import ktb.fullstack.talktalk.domain.chat.entity.ChatRoomMember;
import ktb.fullstack.talktalk.domain.chat.repository.ChatRoomMemberRepository;
import ktb.fullstack.talktalk.domain.chat.repository.ChatRoomRepository;
import ktb.fullstack.talktalk.domain.chat.service.ChatRoomCreator;
import ktb.fullstack.talktalk.domain.user.entity.User;
import ktb.fullstack.talktalk.domain.user.repository.UserRepository;
import ktb.fullstack.talktalk.global.exception.BusinessException;
import ktb.fullstack.talktalk.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
public class ChatRoomCreatorTest {

    @Mock
    ChatRoomRepository chatRoomRepository;

    @Mock
    ChatRoomMemberRepository chatRoomMemberRepository;

    @Mock
    UserRepository userRepository;

    @InjectMocks
    ChatRoomCreator chatRoomCreator;

    private User userFixture(Long id) {
        User user = new User("e" + id + "@a.a", "pw", "n" + id);
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    @Test
    @DisplayName("새 채팅방과 두 참여자를 생성한다")
    void 채팅방과_두_참여자_생성() {

        given(userRepository.findById(1L)).willReturn(Optional.of(userFixture(1L)));
        given(userRepository.findById(99L)).willReturn(Optional.of(userFixture(99L)));
        given(chatRoomRepository.save(any(ChatRoom.class))).willAnswer(inv -> {
            ChatRoom r = inv.getArgument(0);
            ReflectionTestUtils.setField(r, "id", 1L);
            return r;
        });

        ChatRoom room = chatRoomCreator.create("1:99", 1L, 99L);

        assertThat(room.getId()).isEqualTo(1L);
        then(chatRoomMemberRepository).should(times(2)).save(any(ChatRoomMember.class));
    }

    @Test
    @DisplayName("요청자가 존재하지 않으면 INVALID_TOKEN 예외")
    void 요청자_없음() {

        given(userRepository.findById(1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> chatRoomCreator.create("1:99", 1L, 99L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.INVALID_TOKEN);
    }

    @Test
    @DisplayName("상대방이 존재하지 않으면 PARTNER_NOT_FOUND 예외")
    void 상대방_없음() {

        given(userRepository.findById(1L)).willReturn(Optional.of(userFixture(1L)));
        given(userRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> chatRoomCreator.create("1:99", 1L, 99L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.PARTNER_NOT_FOUND);
    }
}
