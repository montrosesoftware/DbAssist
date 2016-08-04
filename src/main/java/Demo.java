import java.util.Date;
import java.util.List;

public class Demo {
    public static void main(String[] args){
        System.out.println("Two sources:");

        HibernateManager hibernateManager = new HibernateManager();
        JDBCManager jdbcManager = new JDBCManager();

        List<User> usersFromHibernate = hibernateManager.getData();
        List<User> usersFromJDBC = jdbcManager.getData();

        System.out.println("Finished");
    }
}
