package ktb.fullstack.talktalk.domain.chat.repository;

import ktb.fullstack.talktalk.domain.chat.entity.ChatRoom;
import ktb.fullstack.talktalk.domain.chat.entity.LastMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ChatRoomUpdater {

    private final MongoTemplate mongoTemplate;

    public void applyLastMessage(UUID roomId, LastMessage lastMessage) {

        Query query = Query.query(Criteria.where("_id").is(roomId)
                .orOperator(
                        Criteria.where("lastMessage").is(null),
                        Criteria.where("lastMessage.id").lt(lastMessage.getId())));

        mongoTemplate.updateFirst(query, new Update().set("lastMessage", lastMessage), ChatRoom.class);
    }

    public void resetLastMessage(UUID roomId, LastMessage lastMessage) {

        Query query = Query.query(Criteria.where("_id").is(roomId));
        Update update = lastMessage == null
                ? new Update().unset("lastMessage")
                : new Update().set("lastMessage", lastMessage);

        mongoTemplate.updateFirst(query, update, ChatRoom.class);
    }

    public void advanceLastRead(UUID roomId, Long userId, UUID messageId) {

        if (messageId == null) return;

        Query query = Query.query(Criteria.where("_id").is(roomId)
                .and("members").elemMatch(Criteria.where("userId").is(userId)
                        .orOperator(
                                Criteria.where("lastReadMessageId").is(null),
                                Criteria.where("lastReadMessageId").lt(messageId))));

        mongoTemplate.updateFirst(query, new Update().set("members.$.lastReadMessageId", messageId), ChatRoom.class);
    }
}
