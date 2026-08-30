package ktb.fullstack.talktalk.domain.chat.repository;

import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class MessageUnreadCounter {

    private final MongoTemplate mongoTemplate;

    public Map<UUID, Long> countByRooms(Map<UUID, UUID> lastReadByRoom, Long userId) {

        if (lastReadByRoom.isEmpty()) return Map.of();

        List<Criteria> branches = lastReadByRoom.entrySet().stream()
                .map(entry -> entry.getValue() == null
                        ? Criteria.where("roomId").is(entry.getKey())
                        : Criteria.where("roomId").is(entry.getKey()).and("_id").gt(entry.getValue()))
                .toList();

        Criteria criteria = Criteria.where("deletedAt").is(null)
                .and("senderId").ne(userId)
                .orOperator(branches.toArray(new Criteria[0]));

        AggregationResults<Document> results = mongoTemplate.aggregate(
                Aggregation.newAggregation(
                        Aggregation.match(criteria),
                        Aggregation.group("roomId").count().as("total")),
                "messages", Document.class);

        Map<UUID, Long> counts = new HashMap<>();
        for (Document row : results) {
            counts.put(row.get("_id", UUID.class), row.get("total", Number.class).longValue());
        }
        return counts;
    }
}
