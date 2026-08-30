package ktb.fullstack.talktalk.domain.chat.demo;

import ktb.fullstack.talktalk.support.MySqlTestContainerConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("mysqltest")
@Import(MySqlTestContainerConfig.class)
class MySqlContainerSmokeTest {

    @Autowired
    DataSource dataSource;

    @Test
    @DisplayName("mysqltest 프로필은 H2가 아니라 실제 InnoDB에 붙는다")
    void 실제_MySQL에_붙는다() throws SQLException {

        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData meta = connection.getMetaData();

            System.out.printf(">>> %s %s%n", meta.getDatabaseProductName(), meta.getDatabaseProductVersion());
            assertThat(meta.getDatabaseProductName()).isEqualTo("MySQL");

            var rs = connection.createStatement().executeQuery(
                    "select engine from information_schema.tables where table_schema = database() and table_name = 'users'");
            assertThat(rs.next()).isTrue();
            assertThat(rs.getString("engine")).isEqualTo("InnoDB");
        }
    }
}
