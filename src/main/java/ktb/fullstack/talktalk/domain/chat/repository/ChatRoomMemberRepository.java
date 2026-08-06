package ktb.fullstack.talktalk.domain.chat.repository;

import ktb.fullstack.talktalk.domain.chat.entity.ChatRoomMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ChatRoomMemberRepository extends JpaRepository<ChatRoomMember, Long> {

    boolean existsByRoomIdAndUserId(Long roomId, Long userId);

    @Query("""
            select count(m) from Message m
            where m.room.id = :roomId and m.sender.id <> :userId
                and (:lastReadMessageId is null or m.id > :lastReadMessageId)
           """)
    long countUnread(@Param("roomId") Long roomId, @Param("userId") Long userId,
                     @Param("lastReadMessageId") Long lastReadMessageId);

    Optional<ChatRoomMember> findByRoomIdAndUserId(Long roomId, Long userId);
}
