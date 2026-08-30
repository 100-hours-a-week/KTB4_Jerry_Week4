package ktb.fullstack.talktalk.domain.chat.repository;

import ktb.fullstack.talktalk.domain.chat.entity.Message;
import org.bson.Document;
import org.springframework.data.mongodb.core.mapping.event.AfterConvertCallback;
import org.springframework.data.mongodb.core.mapping.event.AfterSaveCallback;
import org.springframework.stereotype.Component;

@Component
public class MessageEntityCallbacks implements AfterConvertCallback<Message>, AfterSaveCallback<Message> {

    @Override
    public Message onAfterConvert(Message entity, Document document, String collection) {
        entity.markNotNew();
        return entity;
    }

    @Override
    public Message onAfterSave(Message entity, Document document, String collection) {
        entity.markNotNew();
        return entity;
    }
}
