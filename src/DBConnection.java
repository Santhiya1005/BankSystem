import java.sql.Connection;
import java.sql.DriverManager;
class DBConnection{
    private static final String URL="jdbc:postgresql://localhost:5432/banksystem_db";
    private static final String USER="postgres";
    private static final String PASSWORD="Sandhu1005";
    public static Connection getConnection() throws Exception{
        Connection conn=DriverManager.getConnection(URL,USER,PASSWORD);
        return conn;
    }
}