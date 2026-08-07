package ktb.fullstack.talktalk.domain.chat.unit;

import ktb.fullstack.talktalk.domain.chat.repository.ChatRoomMemberRepository;
import ktb.fullstack.talktalk.domain.chat.repository.MessageRepository;
import ktb.fullstack.talktalk.domain.chat.service.ChatReadService;
import ktb.fullstack.talktalk.global.exception.BusinessException;
import ktb.fullstack.talktalk.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class ChatReadServiceTest {

    @Mock
    ChatRoomMemberRepository chatRoomMemberRepository;

    @Mock
    MessageRepository messageRepository;

    @InjectMocks
    ChatReadService chatReadService;

    @Test
    @DisplayName("getUnreadCount - 채팅방 멤버가 아니면 NOT_CHATROOM_MEMBER 예외")
    void markRead_비멤버_거부() {

        given(chatRoomMemberRepository.findByRoomIdAndUserId(1L, 5L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> chatReadService.markRead(1L, 5L, 20L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.NOT_CHATROOM_MEMBER);
    }

    @Test
    @DisplayName("getUnreadCount - 채팅방 멤버가 아니면 NOT_CHATROOM_MEMBER 예외")
    void unread_비멤버_거부() {

        given(chatRoomMemberRepository.findByRoomIdAndUserId(1L, 5L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> chatReadService.getUnreadCount(1L, 5L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.NOT_CHATROOM_MEMBER);
    }
}
