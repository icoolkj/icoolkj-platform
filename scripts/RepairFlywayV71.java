import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

/** One-off: remove failed Flyway V7.1 record so migration can retry. */
public class RepairFlywayV71 {
    public static void main(String[] args) throws Exception {
        String url = System.getenv().getOrDefault(
                "DB_URL",
                "jdbc:mysql://127.0.0.1:3306/icoolkj_platform_dev?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&useSSL=false");
        String user = System.getenv().getOrDefault("DB_USER", "root");
        String pass = System.getenv("DB_PASS");
        if (pass == null || pass.isEmpty()) {
            throw new IllegalStateException("Set DB_PASS before running this tool. No default credentials are shipped.");
        }

        Class.forName("com.mysql.cj.jdbc.Driver");
        try (Connection conn = DriverManager.getConnection(url, user, pass);
             Statement stmt = conn.createStatement()) {
            try (ResultSet rs = stmt.executeQuery(
                    "SELECT version, success, installed_rank FROM flyway_schema_history WHERE version = '7.1'")) {
                System.out.println("Before repair:");
                while (rs.next()) {
                    System.out.printf("  version=%s success=%s rank=%s%n",
                            rs.getString(1), rs.getBoolean(2), rs.getInt(3));
                }
            }
            int deleted = stmt.executeUpdate(
                    "DELETE FROM flyway_schema_history WHERE version = '7.1' AND success = 0");
            System.out.println("Deleted failed V7.1 records: " + deleted);
        }
    }
}
