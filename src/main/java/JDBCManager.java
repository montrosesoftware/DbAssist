import java.util.*;
import java.sql.*;
import java.util.Date;

public class JDBCManager {

    String connectionUrl;

    private Connection con;
    private Statement stmt;
    private ResultSet rs;

    JDBCManager(){
        connectionUrl = "jdbc:sqlserver://localhost:1433;" +
                "databaseName=test;user=tester;password=test";
    }

    private void initConnectionVars(){
        // Declare the JDBC objects.
        if (rs != null) try { rs.close(); } catch(Exception e) {}
        if (stmt != null) try { stmt.close(); } catch(Exception e) {}
        if (con != null) try { con.close(); } catch(Exception e) {}

        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;
    }

    public List<User> getData() {

        ArrayList<User> users = new ArrayList<User>();

        // Create a variable for the connection string.
        initConnectionVars();

        try {
            // Establish the connection.
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            con = DriverManager.getConnection(connectionUrl);

            // Create and execute an SQL statement that returns some data.
            String SQL = "SELECT Id, Name, CreatedAt FROM Users";
            stmt = con.createStatement();
            rs = stmt.executeQuery(SQL);

            while(rs.next()){
                int id = rs.getInt(1);
                String name = rs.getString(2);
                String s = rs.getString(3);
                //Date date = rs.getTimestamp(3);
                Date dateUTC = DateUtils.getUtc(s);
                users.add(new User(id, name, dateUTC));
            }
        }

        // Handle any errors that may have occurred.
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if (rs != null) try { rs.close(); } catch(Exception e) {}
            if (stmt != null) try { stmt.close(); } catch(Exception e) {}
            if (con != null) try { con.close(); } catch(Exception e) {}
        }

        return users;
    }

    public void writeUserData(User user){

        initConnectionVars();

        try {
            // Establish the connection.
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            con = DriverManager.getConnection(connectionUrl);

            // Create and execute an SQL statement that returns some data.
            String SQL = "INSERT INTO Users (Id, Name, CreatedAt) VALUES (" + user.getId() +", '" + user.getName() + "', '" + DateUtils.getUtc( user.getCreatedAt()) + "')";
            stmt = con.createStatement();
            stmt.executeUpdate(SQL);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if (rs != null) try { rs.close(); } catch(Exception e) {}
            if (stmt != null) try { stmt.close(); } catch(Exception e) {}
            if (con != null) try { con.close(); } catch(Exception e) {}
        }
    }
}
