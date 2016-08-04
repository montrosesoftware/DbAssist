import java.util.Date;
import java.util.List;

public class Demo {
    public static void main(String[] args){
        System.out.println("Two sources:");

        try (JDBCManager jdbcManager = new JDBCManager()) {
            HibernateManager hibernateManager = new HibernateManager();

            List<User> usersFromHibernate = hibernateManager.getData();
            List<User> usersFromJDBC = jdbcManager.getData();
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Finished");
    }
}
