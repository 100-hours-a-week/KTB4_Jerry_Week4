package ktb.fullstack.talktalk.domain.chat.unit;

import ktb.fullstack.talktalk.domain.chat.entity.ChatRoomMember;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import ktb.fullstack.talktalk.domain.chat.entity.ChatRoom;
import ktb.fullstack.talktalk.domain.chat.repository.ChatRoomRepository;
import ktb.fullstack.talktalk.domain.chat.repository.ChatRoomUpdater;

@ExtendWith(MockitoExtension.class)
public class ChatReadServiceTest {

    @Mock
    MessageRepository messageRepository;

    @Mock

    ChatRoomRepository chatRoomRepository;


    @Mock

    ChatRoomUpdater chatRoomUpdater;


    @InjectMocks
    ChatReadService chatReadService;

    private static final UUID ROOM_ID    = UUID.fromString("0198f3a2-7c40-7000-8a3f-1c2d3e4f5060");
    private static final UUID MESSAGE_ID = UUID.fromString("0198f3a2-7c40-7001-9b4e-2d3e4f506170");

    @Test
    @DisplayName("getUnreadCount - 채팅방 멤버가 아니면 NOT_CHATROOM_MEMBER 예외")
    void markRead_비멤버_거부() {

        given(chatRoomRepository.findByIdAndMembersUserId(ROOM_ID, 5L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> chatReadService.markRead(ROOM_ID, 5L, MESSAGE_ID))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.NOT_CHATROOM_MEMBER);
    }

    @Test
    @DisplayName("getUnreadCount - 채팅방 멤버가 아니면 NOT_CHATROOM_MEMBER 예외")
    void unread_비멤버_거부() {

        given(chatRoomRepository.findByIdAndMembersUserId(ROOM_ID, 5L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> chatReadService.getUnreadCount(ROOM_ID, 5L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.NOT_CHATROOM_MEMBER);
    }

    @Test
    @DisplayName("markRead - 채팅방에 없는 메시지 id면 MESSAGE_NOT_FOUND 예외")
    void markRead_없는_메시지_거부() {

        ChatRoom room = ChatRoom.dm("1:2");
        room.addMember(5L);
        given(chatRoomRepository.findByIdAndMembersUserId(ROOM_ID, 5L)).willReturn(Optional.of(room));
        given(messageRepository.existsByIdAndRoomId(MESSAGE_ID, ROOM_ID)).willReturn(false);

        assertThatThrownBy(() -> chatReadService.markRead(ROOM_ID, 5L, MESSAGE_ID))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.MESSAGE_NOT_FOUND);
    }
}
