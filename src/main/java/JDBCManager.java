import java.util.*;
import java.sql.*;
import java.util.Date;

public class JDBCManager {

    public List<User> getData() {

        ArrayList<User> users = new ArrayList<User>();

        // Create a variable for the connection string.
        String connectionUrl = "jdbc:sqlserver://localhost:1433;" +
                "databaseName=test;user=tester;password=test";

        // Declare the JDBC objects.
        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;

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
                Date date = rs.getDate(3);

                users.add(new User(id, name, date));
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
}
