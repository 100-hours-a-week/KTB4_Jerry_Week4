package ktb.fullstack.talktalk.domain.chat.repository;

import java.util.UUID;

public interface RoomUnreadProjection {

    UUID getRoomId();

    long getTotal();
}
