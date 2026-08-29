package ktb.fullstack.talktalk.domain.chat.demo;

import jakarta.persistence.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@Document(collection = "uuid_probe")
public class UuidProbeDocument {

    @Id
    public UUID id;

    public int seq;

    public UuidProbeDocument(UUID id, int seq) {
        this.id = id;
        this.seq = seq;
    }
}
