package ktb.fullstack.talktalk.domain.chat.repository;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.CompoundIndexDefinition;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexOperations;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MongoIndexInitializer {

    private final MongoTemplate mongoTemplate;

    @PostConstruct
    public void createIndexes() {

        IndexOperations messages = mongoTemplate.indexOps("messages");

        messages.createIndex(new Index()
                .on("roomId", Sort.Direction.ASC)
                .on("_id", Sort.Direction.DESC)
                .named("idx_room_message"));

        messages.createIndex(new CompoundIndexDefinition(
                new Document("roomId", 1).append("senderId", 1).append("clientMessageId", 1))
                .named("uq_message_idempotency")
                .unique());
    }
}
