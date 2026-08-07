package ktb.fullstack.talktalk.domain.chat.repository;

import ktb.fullstack.talktalk.domain.chat.entity.ChatRoomMember;
import ktb.fullstack.talktalk.global.common.repository.CountByIdProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
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

    @Query("""
            select m.room.id as roomId, m.user.id as partnerId
            from ChatRoomMember m
            where m.room.id in :roomIds and m.user.id <> :userId
           """)
    List<RoomPartnerProjection> findPartners(@Param("roomIds") List<Long> roomIds, @Param("userId") Long userId);


    @Query("""
            select msg.room.id as id, count(msg) as total
            from Message msg, ChatRoomMember mem
            where mem.room.id = msg.room.id and mem.user.id = :userId
                and mem.room.id in :roomIds
                and msg.sender.id <> :userId
                and (mem.lastReadMessageId is null or msg.id > mem.lastReadMessageId)
            group by msg.room.id
           """)
    List<CountByIdProjection> countUnreadByRooms(@Param("roomIds") List<Long> roomIds, @Param("userId") Long userId);
}
