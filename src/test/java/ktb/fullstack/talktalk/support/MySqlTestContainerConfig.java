package ktb.fullstack.talktalk.support;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.testcontainers.mysql.MySQLContainer;

@Configuration(proxyBeanMethods = false)
public class MySqlTestContainerConfig {

    @Bean
    @ServiceConnection
    public MySQLContainer mysqlContainer() {

        return new MySQLContainer("mysql:8.4").withReuse(true);
    }
}
