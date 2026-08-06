package ktb.fullstack.talktalk.domain.chat.repository;

import ktb.fullstack.talktalk.domain.chat.entity.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MessageRepository extends JpaRepository<Message, Long> {

    Optional<Message> findByRoomIdAndSenderIdAndClientMessageId(Long roomId, Long senderId, String clientMessageId);

    @Query("""
            select m from Message m
            where m.room.id = :roomId and (:cursor is null or m.id <= :cursor)
            order by m.id desc
           """)
    List<Message> findByRoomIdAndCursor(@Param("roomId") Long roomId, @Param("cursor") Long cursor, Pageable pageable);
}
