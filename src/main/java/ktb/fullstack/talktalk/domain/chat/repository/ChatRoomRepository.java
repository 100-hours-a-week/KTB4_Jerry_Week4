package ktb.fullstack.talktalk.domain.chat.repository;

import ktb.fullstack.talktalk.domain.chat.entity.ChatRoom;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChatRoomRepository extends MongoRepository<ChatRoom, UUID> {

    Optional<ChatRoom> findByDmKey(String dmKey);

    boolean existsByIdAndMembersUserId(UUID id, Long userId);

    Optional<ChatRoom> findByIdAndMembersUserId(UUID id, Long userId);

    @Query("{ 'members.userId': ?0, 'lastMessage.id': { $ne: null } }")
    List<ChatRoom> findRoomsByMember(Long userId, Pageable pageable);

    @Query("{ 'members.userId': ?0, 'lastMessage.id': { $ne: null, $lte: ?1 } }")
    List<ChatRoom> findRoomsByMemberAndCursor(Long userId, UUID cursor, Pageable pageable);
}
