package ktb.fullstack.talktalk.domain.chat.repository;

import ktb.fullstack.talktalk.domain.chat.entity.ChatRoomMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChatRoomMemberRepository extends JpaRepository<ChatRoomMember, Long> {

    boolean existsByRoomIdAndUserId(UUID roomId, Long userId);

    Optional<ChatRoomMember> findByRoomIdAndUserId(UUID roomId, Long userId);

    @Query("""
            select m.room.id as roomId, m.user.id as partnerId
            from ChatRoomMember m
            where m.room.id in :roomIds and m.user.id <> :userId
           """)
    List<RoomPartnerProjection> findPartners(@Param("roomIds") List<UUID> roomIds, @Param("userId") Long userId);


    @Query("""
            select msg.room.id as roomId, count(msg) as total
            from Message msg, ChatRoomMember mem
            where mem.room.id = msg.room.id and mem.user.id = :userId
                and mem.room.id in :roomIds
                and msg.sender.id <> :userId
                and msg.deletedAt is null
                and (mem.lastReadMessageId is null or msg.id > mem.lastReadMessageId)
            group by msg.room.id
           """)
    List<RoomUnreadProjection> countUnreadByRooms(@Param("roomIds") List<UUID> roomIds, @Param("userId") Long userId);
}
