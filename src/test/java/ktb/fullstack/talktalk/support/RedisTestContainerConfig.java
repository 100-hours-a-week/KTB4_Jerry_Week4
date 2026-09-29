package ktb.fullstack.talktalk.support;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.testcontainers.containers.GenericContainer;

@Configuration(proxyBeanMethods = false)
public class RedisTestContainerConfig {

    @Bean
    @ServiceConnection(name = "redis")
    public GenericContainer<?> redisContainer() {

        return new GenericContainer<>("redis:alpine").withExposedPorts(6379);
    }
}
