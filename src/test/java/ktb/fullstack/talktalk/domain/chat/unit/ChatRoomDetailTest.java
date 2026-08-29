package ktb.fullstack.talktalk.domain.chat.unit;

import ktb.fullstack.talktalk.domain.chat.repository.ChatRoomMemberRepository;
import ktb.fullstack.talktalk.domain.chat.repository.ChatRoomRepository;
import ktb.fullstack.talktalk.domain.chat.service.ChatRoomQueryService;
import ktb.fullstack.talktalk.global.exception.BusinessException;
import ktb.fullstack.talktalk.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class ChatRoomDetailTest {

    @Mock
    ChatRoomRepository chatRoomRepository;

    @Mock
    ChatRoomMemberRepository chatRoomMemberRepository;

    @InjectMocks
    ChatRoomQueryService chatRoomQueryService;

    private static final UUID ROOM_ID = UUID.fromString("0198f3a2-7c40-7000-8a3f-1c2d3e4f5060");

    @Test
    @DisplayName("존재하지 않는 채팅방을 조회하면 CHATROOM_NOT_FOUND 예외")
    void 없는_방() {

        given(chatRoomRepository.existsById(ROOM_ID)).willReturn(false);

        assertThatThrownBy(() -> chatRoomQueryService.getRoom(ROOM_ID, 10L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.CHATROOM_NOT_FOUND);
    }

    @Test
    @DisplayName("채팅방은 존재하지만 멤버가 아니면 NOT_CHATROOM_MEMBER 예외")
    void 비멤버() {

        given(chatRoomRepository.existsById(ROOM_ID)).willReturn(true);
        given(chatRoomMemberRepository.existsByRoomIdAndUserId(ROOM_ID, 10L)).willReturn(false);

        assertThatThrownBy(() -> chatRoomQueryService.getRoom(ROOM_ID, 10L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.NOT_CHATROOM_MEMBER);
    }
}
