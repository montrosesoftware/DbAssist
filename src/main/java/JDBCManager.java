import java.util.*;
import java.sql.*;
import java.util.Date;

public class JDBCManager implements AutoCloseable {

    private String connectionUrl;

    private Connection con;
    private Statement stmt;
    private ResultSet rs;

    JDBCManager() throws ClassNotFoundException, SQLException{
        //prepare and establish a connection
        connectionUrl = "jdbc:sqlserver://localhost:1433;" +
                "databaseName=test;user=tester;password=test";

        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        con = DriverManager.getConnection(connectionUrl);
    }

    public List<User> getData() throws SQLException{

        ArrayList<User> users = new ArrayList<User>();

        // Create and execute an SQL statement that returns users data
        String SQL = "SELECT Id, Name, CreatedAt FROM Users";
        stmt = con.createStatement();
        rs = stmt.executeQuery(SQL);

        while(rs.next()){
            int id = rs.getInt(1);
            String name = rs.getString(2);
            String dateStr = rs.getString(3);
            Date dateUTC = DateUtils.getUtc(dateStr);
            users.add(new User(id, name, dateUTC));
        }
        return users;
    }

    public void writeUserData(User user) throws SQLException{

        // Create and execute an SQL statement that returns some data.
        String SQL = "INSERT INTO Users (Id, Name, CreatedAt) VALUES (" + user.getId() +", '" + user.getName() + "', '" + DateUtils.getUtc(user.getCreatedAt()) + "')";
        stmt = con.createStatement();
        stmt.executeUpdate(SQL);
    }

    public void close() throws Exception {
        if (rs != null) try { rs.close(); } catch(Exception e) {}
        if (stmt != null) try { stmt.close(); } catch(Exception e) {}
        if (con != null) try { con.close(); } catch(Exception e) {}
    }
}
