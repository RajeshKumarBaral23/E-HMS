package hospital.tools;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class RunSqlMigration {
    public static void main(String[] args) throws Exception {
        if (args.length < 4) {
            System.err.println("Usage: RunSqlMigration <jdbcUrl> <user> <password> <sqlFile>");
            System.exit(2);
        }
        String url = args[0];
        String user = args[1];
        String password = args[2];
        String sqlFile = args[3];

        String sql = new String(Files.readAllBytes(Paths.get(sqlFile)), java.nio.charset.StandardCharsets.UTF_8);

        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement st = conn.createStatement()) {
            conn.setAutoCommit(false);
            String[] parts = sql.split(";\n");
            for (String part : parts) {
                String stmt = part.trim();
                if (stmt.isEmpty()) continue;
                if (stmt.endsWith(";")) stmt = stmt.substring(0, stmt.length()-1);
                System.out.println("Executing: " + (stmt.length() > 120 ? stmt.substring(0, 120) + "..." : stmt));
                st.execute(stmt);
            }
            conn.commit();
            System.out.println("SQL migration executed successfully: " + sqlFile);
        }
    }
}
