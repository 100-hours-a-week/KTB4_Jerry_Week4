package ktb.fullstack.talktalk.domain.chat.repository;

import ktb.fullstack.talktalk.domain.chat.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    Optional<ChatRoom> findByDmKey(String dmKey);
}
