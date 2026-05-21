package dal;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBContext {

    private static final String URL;
    private static final String USER;
    private static final String PASSWORD;

    static {
        try {
            Properties props = new Properties();
            InputStream input = DBContext.class
                    .getClassLoader()
                    .getResourceAsStream("config/config.properties");

            if (input == null) {
                throw new RuntimeException("Cannot find ConnectDB.properties");
            }

            props.load(input);
            URL = props.getProperty("db.url");
            USER = props.getProperty("db.user");
            PASSWORD = props.getProperty("db.password");

            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

        } catch (ClassNotFoundException | IOException e) {
            throw new RuntimeException("Database init failed", e);
        }
    }

    private DBContext() {
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
