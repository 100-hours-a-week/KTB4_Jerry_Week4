package ktb.fullstack.talktalk.domain.chat.repository;

import ktb.fullstack.talktalk.domain.chat.entity.ChatRoomMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRoomMemberRepository extends JpaRepository<ChatRoomMember, Long> {

    boolean existsByRoomIdAndUserId(Long roomId, Long userId);
}
