package growth_tracker.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConfig {
    private static final String URL = env("DB_URL", "jdbc:postgresql://localhost:5432/growth_tracker");
    private static final String USERNAME = env("DB_USER", "postgres");
    private static final String PASSWORD = env("DB_PASSWORD", "postgres");

    private static String env(String name, String fallback) {
        String value = System.getenv(name);
        return value != null ? value : fallback;
    }

    private DatabaseConfig() {
    }

    public static Connection getConnection()
            throws SQLException {

        return DriverManager.getConnection(
                URL,
                USERNAME,
                PASSWORD
        );
    }
}
