package ktb.fullstack.talktalk.domain.chat.repository;

import ktb.fullstack.talktalk.domain.chat.entity.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MessageRepository extends MongoRepository<Message, UUID> {

    Optional<Message> findByRoomIdAndSenderIdAndClientMessageId(UUID roomId, Long senderId, String clientMessageId);

    List<Message> findByRoomIdOrderByIdDesc(UUID roomId, Pageable pageable);

    List<Message> findByRoomIdAndIdLessThanEqualOrderByIdDesc(UUID roomId, UUID cursor, Pageable pageable);

    long countByRoomIdAndSenderIdNotAndDeletedAtIsNull(UUID roomId, Long senderId);

    long countByRoomIdAndSenderIdNotAndDeletedAtIsNullAndIdGreaterThan(UUID roomId, Long senderId, UUID lastReadMessageId);

    Optional<Message> findTopByRoomIdAndDeletedAtIsNullOrderByIdDesc(UUID roomId);

    boolean existsByIdAndRoomId(UUID id, UUID roomId);
}
