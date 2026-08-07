package ktb.fullstack.talktalk.domain.chat.unit;

import ktb.fullstack.talktalk.domain.chat.entity.ChatRoom;
import ktb.fullstack.talktalk.domain.chat.repository.ChatRoomRepository;
import ktb.fullstack.talktalk.domain.chat.service.ChatRoomCreator;
import ktb.fullstack.talktalk.domain.chat.service.ChatRoomService;
import ktb.fullstack.talktalk.global.common.response.CreateResponseDto;
import ktb.fullstack.talktalk.global.exception.BusinessException;
import ktb.fullstack.talktalk.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;


@ExtendWith(MockitoExtension.class)
public class ChatRoomServiceTest {

    @Mock
    ChatRoomRepository chatRoomRepository;

    @Mock
    ChatRoomCreator chatRoomCreator;

    @InjectMocks
    ChatRoomService chatRoomService;

    private ChatRoom roomFixture(Long id, String dmKey) {

        ChatRoom room = ChatRoom.dm(dmKey);
        ReflectionTestUtils.setField(room, "id", id);
        return room;
    }

    @Test
    @DisplayName("기존 채팅방이 있으면 그대로 반환하고 새로 만들지 않는다")
    void 채팅방_이미_존재하면_그대로_반환() {

        given(chatRoomRepository.findByDmKey("1:99")).willReturn(Optional.of(roomFixture(1L, "1:99")));

        CreateResponseDto result = chatRoomService.getOrCreateDm(1L, 99L);

        assertThat(result.getId()).isEqualTo(1L);
        then(chatRoomCreator).should(never()).create(any(), any(), any());
    }

    @Test
    @DisplayName("DM 채팅방이 없으면 두 참여자가 포함된 새 채팅방을 생성한다")
    void 채팅방_없으면_새_채팅방_생성() {

        given(chatRoomRepository.findByDmKey("1:99")).willReturn(Optional.empty());
        given(chatRoomCreator.create("1:99", 1L, 99L)).willReturn(roomFixture(1L, "1:99"));

        CreateResponseDto result = chatRoomService.getOrCreateDm(1L, 99L);

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("자기 자신과는 DM 채팅방을 만들 수 없다")
    void 자신과의_채팅_불가() {

        assertThatThrownBy(() -> chatRoomService.getOrCreateDm(2L, 2L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.CANNOT_CHAT_ALONE);

        then(chatRoomRepository).should(never()).findByDmKey(any());
        then(chatRoomCreator).should(never()).create(any(), any(), any());
    }

    @Test
    @DisplayName("동시 생성 Race Condition으로 DB Unique 위반이 나면 기존 방을 조회해 반환한다")
    void 경쟁_조건_복구() {

        given(chatRoomRepository.findByDmKey("1:99"))
                .willReturn(Optional.empty())
                .willReturn(Optional.of(roomFixture(1L, "1:99")));
        given(chatRoomCreator.create("1:99", 1L, 99L))
                .willThrow(new DataIntegrityViolationException("dup dm_key"));

        CreateResponseDto result = chatRoomService.getOrCreateDm(1L, 99L);

        assertThat(result.getId()).isEqualTo(1L);
    }
}
