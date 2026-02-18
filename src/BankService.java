import java.math.BigDecimal;
import java.sql.*;
import java.util.Scanner;

public class BankService {
    public void checkBalance(int accountNo) {
        try {
            Connection conn = DBConnection.getConnection();
            String sql = "SELECT c.name,a.account_no,a.account_type,a.balance FROM accounts a JOIN customers c ON a.customer_id=c.customer_id WHERE a.account_no=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, accountNo);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String name = rs.getString("name");
                int accNo = rs.getInt("account_no");
                String type = rs.getString("account_type");
                BigDecimal balance = rs.getBigDecimal("balance");
                System.out.println("Name: " + name + ", Account No: SBI-" + accNo + ", Account Type: " + type + ", Your Current Balance: " + balance);
            } else {
                System.out.println("Account Not Found");
            }
            rs.close();
            ps.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void Deposite(int accountNo, BigDecimal amount) {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);
            //find account_id
            int accountId=AccountId.getAccountId(conn,accountNo);
            if(accountId==-1){
                System.out.println("Account Not Found");
                conn.rollback();
                return;
            }
            //insert transaction into transaction table
            String insertSql = "INSERT INTO transactions (transaction_type, account_id, amount, transaction_date) VALUES (?, ?, ?, NOW())";
            PreparedStatement insertps = conn.prepareStatement(insertSql);
            insertps.setString(1, "DEPOSIT");
            insertps.setInt(2, accountId);
            insertps.setBigDecimal(3, amount);
            insertps.executeUpdate();

            //update balance
            String updateSql = "UPDATE accounts SET balance=balance+? WHERE account_id=?";
            PreparedStatement updateps = conn.prepareStatement(updateSql);
            updateps.setBigDecimal(1, amount);
            updateps.setInt(2, accountId);
            updateps.executeUpdate();
            conn.commit();
            System.out.println("Deposit successful");

            //showing current balance
            String currentSql = "SELECT c.name,a.account_no,a.account_type,a.balance FROM accounts a JOIN customers c ON c.customer_id=a.customer_id WHERE a.account_id=?";
            PreparedStatement currentps = conn.prepareStatement(currentSql);
            currentps.setInt(1, accountId);
            ResultSet currentrs = currentps.executeQuery();
            if (currentrs.next()) {
                String name = currentrs.getString("name");
                String Type = currentrs.getString("account_type");
                Double bal = currentrs.getDouble("balance");
                System.out.println("Name: " + name + ", Account No: SBI-" + accountNo + ", Account_type: " + Type + ", Your Current Balance: " + bal);
            }
            insertps.close();
            updateps.close();
            currentps.close();
            currentrs.close();
        } catch (Exception e) {
            e.printStackTrace();

            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        } finally {
            try {
                if (conn != null) conn.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }

    }

    public void Withdraw(int accountNo, BigDecimal amount) {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);
            //find current balacne
            String findSql = "SELECT balance FROM accounts WHERE account_no=?";
            PreparedStatement findps = conn.prepareStatement(findSql);
            findps.setInt(1, accountNo);
            ResultSet findrs = findps.executeQuery();

            if (!findrs.next()) {
                System.out.println("Balance not found for this account");
                findrs.close();
                findps.close();
                conn.rollback();
                return;
            }

            BigDecimal curr_bal = findrs.getBigDecimal("balance");
            findrs.close();
            findps.close();
            if (curr_bal.compareTo(amount) < 0) {
                System.out.println("Your Current Balance is Not Sufficient to withdraw and your current balance is: " + curr_bal);
                findrs.close();
                findps.close();
                conn.rollback();
                return;
            }
            //find account_id
            int accountId=AccountId.getAccountId(conn,accountNo);
            if(accountId==-1){
                System.out.println("Account Not Found");
                conn.rollback();
                return;
            }
            //insert into transaction table
            String insertSql = "INSERT INTO transactions (transaction_type, account_id, amount, transaction_date) VALUES (?, ?, ?, NOW())";
            PreparedStatement insertps = conn.prepareStatement(insertSql);
            insertps.setString(1, "WITHDRAW");
            insertps.setInt(2, accountId);
            insertps.setBigDecimal(3, amount);
            insertps.executeUpdate();

            //update the balance
            String updateSql = "UPDATE accounts SET balance=balance-? WHERE account_id=?";
            PreparedStatement updateps = conn.prepareStatement(updateSql);
            updateps.setBigDecimal(1, amount);
            updateps.setInt(2, accountId);
            updateps.executeUpdate();
            conn.commit();
            System.out.println("Withdraw Completed");

            //showing current balance
            String currentSql = "SELECT c.name,a.account_no,a.account_type,a.balance FROM accounts a JOIN customers c ON c.customer_id=a.customer_id WHERE a.account_id=?";
            PreparedStatement currentps = conn.prepareStatement(currentSql);
            currentps.setInt(1, accountId);
            ResultSet currentrs = currentps.executeQuery();
            if (currentrs.next()) {
                String name = currentrs.getString("name");
                String type = currentrs.getString("account_type");
                double bal = currentrs.getDouble("balance");
                System.out.println("Name: " + name + ", Account No: SBI-" + accountNo + ", Account type: " + type + ", Your Current Balance: " + bal);
            } else {
                System.out.println("Failed");
            }
            findrs.close();
            findps.close();
            insertps.close();
            updateps.close();
            currentps.close();
            currentrs.close();
        } catch (Exception e) {
            e.printStackTrace();

            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        } finally {
            try {
                if (conn != null) conn.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void ShowTransaction(int accountNo) {
        try {
            Connection conn = DBConnection.getConnection();
            //find account_id
            int accountId=AccountId.getAccountId(conn,accountNo);
            if(accountId==-1){
                System.out.println("Account Not Found");
                return;
            }
            //show
            String showSql = "SELECT transaction_id,transaction_type,amount, transaction_date FROM transactions WHERE account_id=? ORDER BY transaction_date DESC";
            PreparedStatement ps = conn.prepareStatement(showSql);
            ps.setInt(1, accountId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("transaction_id");
                String type = rs.getString("transaction_type");
                BigDecimal amount = rs.getBigDecimal("amount");
                Timestamp date = rs.getTimestamp("transaction_date");
                System.out.println("Transaction Id: " + id + ", Account No: SBI-" + accountNo + ", Transaction Type: " + type + ", Amount: " + amount + ", Date: " + date);
            }
            rs.close();
            ps.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void CreateAcc(String name, String phone, String email, String type) {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);
            String insertsql = "INSERT INTO customers(name,phone,email) VALUES (?,?,?) RETURNING customer_id";
            PreparedStatement ps = conn.prepareStatement(insertsql);
            ps.setString(1, name);
            ps.setString(2, phone);
            ps.setString(3, email);
            ResultSet rs = ps.executeQuery();
            rs.next();
            int customerId = rs.getInt("customer_id");
            rs.close();
            ps.close();
            //insert into account table
            String sql = "INSERT INTO accounts(customer_id,balance,account_type) VALUES (?,0,?) RETURNING account_no";
            PreparedStatement insertps = conn.prepareStatement(sql);
            insertps.setInt(1, customerId);
            insertps.setString(2, type);
            ResultSet accRs = insertps.executeQuery();
            accRs.next();
            long accountNo = accRs.getLong("account_no");

            accRs.close();
            insertps.close();
            conn.commit();
            System.out.println("Your Account is Created");
            System.out.println("Your Name: " + name + " ,Your Account No: " + accountNo + " ,Account Type: " + type);
        }
        catch(Exception e){

                try {
                    if (conn != null) conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }

                e.printStackTrace();

            } finally{
                try {
                    if (conn != null) conn.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
    }
    public void transfer(int fromAccNo, int toAccNo, BigDecimal amount) {
        Connection conn = null;

        try {
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                System.out.println("Invalid amount. Must be > 0");
                return;
            }
            if (fromAccNo == toAccNo) {
                System.out.println("From and To accounts cannot be same");
                return;
            }

            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            // 1) Debit (atomic)
            String debitSql = """
                UPDATE accounts
                SET balance = balance - ?
                WHERE account_no = ? AND balance >= ?
                """;
            try (PreparedStatement debitPs = conn.prepareStatement(debitSql)) {
                debitPs.setBigDecimal(1, amount);
                debitPs.setInt(2, fromAccNo);
                debitPs.setBigDecimal(3, amount);

                int debited = debitPs.executeUpdate();
                if (debited == 0) {
                    System.out.println("Insufficient balance or From-Account not found");
                    conn.rollback();
                    return;
                }
            }

            // 2) Credit
            String creditSql = """
                UPDATE accounts
                SET balance = balance + ?
                WHERE account_no = ?
                """;
            try (PreparedStatement creditPs = conn.prepareStatement(creditSql)) {
                creditPs.setBigDecimal(1, amount);
                creditPs.setInt(2, toAccNo);

                int credited = creditPs.executeUpdate();
                if (credited == 0) {
                    System.out.println("To-Account not found");
                    conn.rollback();
                    return;
                }
            }

            // 3) Transaction logs (need account_id for both)
            int fromId = AccountId.getAccountId(conn, fromAccNo);
            int toId   = AccountId.getAccountId(conn, toAccNo);

            if (fromId == -1 || toId == -1) {
                System.out.println("Account mapping failed");
                conn.rollback();
                return;
            }

            String txnSql = """
                INSERT INTO transactions (transaction_type, account_id, amount, transaction_date)
                VALUES (?, ?, ?, NOW())
                """;
            try (PreparedStatement txnPs = conn.prepareStatement(txnSql)) {
                // DEBIT record
                txnPs.setString(1, "TRANSFER_DEBIT");
                txnPs.setInt(2, fromId);
                txnPs.setBigDecimal(3, amount);
                txnPs.executeUpdate();

                // CREDIT record
                txnPs.setString(1, "TRANSFER_CREDIT");
                txnPs.setInt(2, toId);
                txnPs.setBigDecimal(3, amount);
                txnPs.executeUpdate();
            }

            conn.commit();
            System.out.println("Transfer successful " + amount + " from SBI-" + fromAccNo + " to SBI-" + toAccNo);
            String currentSql = "SELECT c.name,a.account_no,a.account_type,a.balance FROM accounts a JOIN customers c ON c.customer_id=a.customer_id WHERE a.account_id=?";
            PreparedStatement currentps = conn.prepareStatement(currentSql);
            currentps.setInt(1, fromId);
            ResultSet currentrs = currentps.executeQuery();
            if (currentrs.next()) {
                String name = currentrs.getString("name");
                String type = currentrs.getString("account_type");
                double bal = currentrs.getDouble("balance");
                System.out.println("Name: " + name + ", Account No: SBI-" + fromId + ", Account type: " + type + ", Your Current Balance: " + bal);
            } else {
                System.out.println("Failed");
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        } finally {
            try {
                if (conn != null) conn.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void menu(){

        int acc_no;
        BigDecimal amount;
        boolean flag=true;
        Scanner scan=new Scanner(System.in);
        while(flag) {
            System.out.println("1.Create Account \n2.Check Balance \n3.Deposite amount \n4.Withdraw amount \n5.Transaction History\n6.Transfer money\n7.exit");
            int option = scan.nextInt();
            switch (option) {
                case 1:
                    System.out.println("Creating Your Account.....please enter your details");
                    System.out.println("Enter Your Name: ");
                    String name = scan.next();
                    System.out.println("Enter Your Phone No: ");
                    String phone = scan.next();
                    System.out.println("Enter Your Email: ");
                    String email = scan.next();
                    System.out.println("Enter Your Account Type(CURRENT/SAVINGS): ");
                    String type = scan.next();
                    CreateAcc(name, phone, email, type);
                    break;
                case 2:
                    System.out.println("Enter Your Account No(Recheck before enter): ");
                    acc_no = scan.nextInt();
                    checkBalance(acc_no);
                    break;
                case 3:
                    System.out.println("Enter Your Account No(Recheck before enter): ");
                    acc_no = scan.nextInt();
                    System.out.println("Enter Amount(>0): ");
                    amount = scan.nextBigDecimal();
                    if (amount.compareTo(BigDecimal.ZERO) > 0) {
                        Deposite(acc_no,amount);
                    } else {
                        System.out.println("Invalid amount. Please enter valid amount");
                    }
                    break;
                case 4:
                    System.out.println("Enter Your Account No(Recheck before enter): ");
                    acc_no = scan.nextInt();
                    System.out.println("Enter Amount(>0): ");
                    amount = scan.nextBigDecimal();
                    if (amount.compareTo(BigDecimal.ZERO) > 0) {
                        Withdraw(acc_no,amount);
                    } else {
                        System.out.println("Invalid amount. Please enter valid amount");
                    }
                    break;
                case 5:
                    System.out.println("Enter Your Account No(Recheck before enter): ");
                    acc_no = scan.nextInt();
                    ShowTransaction(acc_no);
                    break;
                case 6:
                    System.out.println("Enter From Account No:");
                    int fromAcc = scan.nextInt();
                    System.out.println("Enter To Account No:");
                    int toAcc = scan.nextInt();
                    System.out.println("Enter Amount (>0):");
                    BigDecimal amt = scan.nextBigDecimal();

                    transfer(fromAcc, toAcc, amt);
                    break;

                case 7:
                    flag=false;
                    System.out.println("Thank You for using our bank");
                    break;
                default:
                    System.out.println("Enter valid option");
            }
        }
    }
}