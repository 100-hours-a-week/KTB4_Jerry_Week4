package ktb.fullstack.talktalk.support;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.testcontainers.mongodb.MongoDBContainer;

@Configuration(proxyBeanMethods = false)
public class MongoTestContainerConfig {

    @Bean
    @ServiceConnection
    public MongoDBContainer mongoDBContainer() {

        return new MongoDBContainer("mongo:8");
    }
}
