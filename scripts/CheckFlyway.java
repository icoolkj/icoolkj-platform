import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

/** Local diagnostics only. All credentials come from environment variables. */
public class CheckFlyway {
    public static void main(String[] args) throws Exception {
        String url = System.getenv().getOrDefault("DB_URL",
                "jdbc:mysql://127.0.0.1:3306/icoolkj_platform_dev?useSSL=false");
        String user = System.getenv().getOrDefault("DB_USER", "root");
        String pass = System.getenv("DB_PASS");
        if (pass == null || pass.isEmpty()) {
            throw new IllegalStateException("Set DB_PASS before running this tool. No default credentials are shipped.");
        }

        Class.forName("com.mysql.cj.jdbc.Driver");
        try (Connection conn = DriverManager.getConnection(url, user, pass);
             Statement stmt = conn.createStatement()) {
            System.out.println("Flyway history (latest 5):");
            try (ResultSet rs = stmt.executeQuery(
                    "SELECT version, success FROM flyway_schema_history ORDER BY installed_rank DESC LIMIT 5")) {
                while (rs.next()) {
                    System.out.printf("  version=%s success=%s%n", rs.getString(1), rs.getBoolean(2));
                }
            }
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(1) FROM sys_i18n_message")) {
                rs.next();
                System.out.println("sys_i18n_message count: " + rs.getInt(1));
            }
        }
    }
}
