import java.sql.*;

public class AccountId {
    public static int getAccountId(Connection conn, int accountNo) {
        String sql = "SELECT account_id FROM accounts WHERE account_no=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, accountNo);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("account_id");
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
}
