package ktb.fullstack.talktalk.domain.chat.demo;

import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.NoArgGenerator;
import ktb.fullstack.talktalk.support.MongoTestContainerConfig;
import ktb.fullstack.talktalk.support.MySqlTestContainerConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("mongotest")
@Import({ MySqlTestContainerConfig.class, MongoTestContainerConfig.class })
public class MongoUuidSmokeTest {

    private static final NoArgGenerator GENERATOR = Generators.timeBasedEpochGenerator();
    private static final int COUNT = 50;

    @Autowired
    MongoTemplate mongoTemplate;

    @Test
    @DisplayName("UUIDv7을 _id로 저장하면 바이트 순서가 보존되어 시간순 정렬이 유지된다")
    void uuid_정렬_보존() throws InterruptedException {

        mongoTemplate.dropCollection(UuidProbeDocument.class);

        for (int i = 0; i < COUNT; i++) {
            mongoTemplate.save(new UuidProbeDocument(GENERATOR.generate(), i));
            if (i % 10 == 0) Thread.sleep(2);
        }

        List<UuidProbeDocument> sorted = mongoTemplate.find(
                new Query().with(Sort.by("_id")),
                UuidProbeDocument.class);

        assertThat(sorted).hasSize(COUNT);
        assertThat(sorted).extracting(doc -> doc.seq).isSorted();
    }
}
