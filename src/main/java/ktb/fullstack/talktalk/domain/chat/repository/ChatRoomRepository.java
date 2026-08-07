package ktb.fullstack.talktalk.domain.chat.repository;

import ktb.fullstack.talktalk.domain.chat.entity.ChatRoom;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    Optional<ChatRoom> findByDmKey(String dmKey);

    @Query("""
            select r from ChatRoom r
            join ChatRoomMember m on m.room = r
            where m.user.id = :userId and r.lastMessageId is not null
                and (:cursor is null or r.lastMessageId <= :cursor)
            order by r.lastMessageId desc
           """)
    List<ChatRoom> findRoomsByMemberAndCursor(@Param("userId") Long userId,
                                              @Param ("cursor") Long cursor, Pageable pageable);
}
