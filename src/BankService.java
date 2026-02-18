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
                double balance = rs.getDouble("balance");
                System.out.println("Name: " + name + ", Account No: " + accNo + ", Account Type: " + type + ", Your Current Balance: " + balance);
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

    public void Deposite(int accountNo, double amount) {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);
            String findSql = "SELECT account_id FROM accounts WHERE account_no=?";
            PreparedStatement findps = conn.prepareStatement(findSql);
            findps.setInt(1, accountNo);
            ResultSet findrs = findps.executeQuery();
            int accountId;
            if (findrs.next()) {
                accountId = findrs.getInt("account_id");
            } else {
                System.out.println("Account Not Found");
                return;
            }

            //insert transaction into transaction table
            String insertSql = "INSERT INTO transactions (transaction_type, account_id, amount, transaction_date) VALUES (?, ?, ?, NOW())";
            PreparedStatement insertps = conn.prepareStatement(insertSql);
            insertps.setString(1, "DEPOSIT");
            insertps.setInt(2, accountId);
            insertps.setDouble(3, amount);
            insertps.executeUpdate();

            //update balance
            String updateSql = "UPDATE accounts SET balance=balance+? WHERE account_id=?";
            PreparedStatement updateps = conn.prepareStatement(updateSql);
            updateps.setDouble(1, amount);
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
                System.out.println("Name: " + name + ", Account No: " + accountNo + ", Account_type: " + Type + ", Your Current Balance: " + bal);
            }
            findrs.close();
            findps.close();
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

    public void Withdraw(int accountNo, double amount) {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            //find account id
            String findSql = "SELECT account_id,balance FROM accounts WHERE account_no=?";
            PreparedStatement findps = conn.prepareStatement(findSql);
            findps.setInt(1, accountNo);
            ResultSet findrs = findps.executeQuery();
            int accountId;
            double curr_bal;
            if (findrs.next()) {
                accountId = findrs.getInt("account_id");
                curr_bal = findrs.getDouble("balance");
                if (curr_bal < amount) {
                    System.out.println("Your Current Balance is Not Sufficient to withdraw and your current balance is: " + curr_bal);
                    return;
                }
            } else {
                System.out.println("Account Not Found");
                return;
            }

            //insert into transaction table
            String insertSql = "INSERT INTO transactions (transaction_type, account_id, amount, transaction_date) VALUES (?, ?, ?, NOW())";
            PreparedStatement insertps = conn.prepareStatement(insertSql);
            insertps.setString(1, "WITHDRAW");
            insertps.setInt(2, accountId);
            insertps.setDouble(3, amount);
            insertps.executeUpdate();

            //update the balance
            String updateSql = "UPDATE accounts SET balance=balance-? WHERE account_id=?";
            PreparedStatement updateps = conn.prepareStatement(updateSql);
            updateps.setDouble(1, amount);
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
                System.out.println("Name: " + name + ", Account No: " + accountNo + ", Account type: " + type + ", Your Current Balance: " + bal);
            } else {
                System.out.println("Failed");
            }
            findrs.close();
            findps.close();
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
            String findSql = "SELECT account_id,balance FROM accounts WHERE account_no=?";
            PreparedStatement findps = conn.prepareStatement(findSql);
            findps.setInt(1, accountNo);
            ResultSet findrs = findps.executeQuery();
            int accountId;
            double curr_bal;
            if (findrs.next()) {
                accountId = findrs.getInt("account_id");
            } else {
                System.out.println("Account Not Found");
                return;
            }

            //show
            String showSql = "SELECT transaction_id,transaction_type,amount, transaction_date FROM transactions WHERE account_id=?";
            PreparedStatement ps = conn.prepareStatement(showSql);
            ps.setInt(1, accountId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("transaction_id");
                String type = rs.getString("transaction_type");
                double amount = rs.getDouble("amount");
                Timestamp date = rs.getTimestamp("transaction_date");
                System.out.println("Transaction Id: " + id + ", Account No: " + accountNo + ", Transaction Type: " + type + ", Amount: " + amount + ", Date: " + date);
            }
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
    public void menu(){

        int acc_no;
        double amount;
        boolean flag=true;
        while(flag) {
            System.out.println("1.Create Account \n2.Check Balance \n3.Deposite amount \n4.Withdraw amount \n5.Transaction History\n6.exit");
            Scanner scan=new Scanner(System.in);
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
                    amount = scan.nextDouble();
                    if (amount > 0) {
                        Deposite(acc_no,amount);
                    } else {
                        System.out.println("Invalid amount. Please enter valid amount");
                    }
                    break;
                case 4:
                    System.out.println("Enter Your Account No(Recheck before enter): ");
                    acc_no = scan.nextInt();
                    System.out.println("Enter Amount(>0): ");
                    amount = scan.nextDouble();
                    if (amount > 0) {
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
                    flag=false;
                    System.out.println("Thank You for using our bank");
                    break;
                default:
                    System.out.println("Enter valid option");
            }
        }
    }
}