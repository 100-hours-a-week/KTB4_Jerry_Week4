package ktb.fullstack.talktalk.support;

import org.testcontainers.mysql.MySQLContainer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

public class InnodbInspector {

    private final MySQLContainer container;

    public InnodbInspector(MySQLContainer container) {
        this.container = container;
    }

    public String latestDeadlock() {

        String status = engineStatus();
        int from = status.indexOf("LATEST DETECTED DEADLOCK");
        if (from < 0) return "(감지된 데드락 없음)";

        int to = status.indexOf("TRANSACTIONS", from);
        return status.substring(from, to < 0 ? status.length() : to);
    }

    public long deadlockCount() {

        try (Connection connection = rootConnection()) {
            ResultSet rs = connection.createStatement().executeQuery(
                    "select count from information_schema.innodb_metrics where name = 'lock_deadlocks'");
            return rs.next() ? rs.getLong(1) : -1;
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    private String engineStatus() {

        try (Connection connection = rootConnection()) {
            ResultSet rs = connection.createStatement().executeQuery("show engine innodb status");
            rs.next();
            return rs.getString("Status");
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    private Connection rootConnection() throws SQLException {

        return DriverManager.getConnection(container.getJdbcUrl(), "root", container.getPassword());
    }
}
