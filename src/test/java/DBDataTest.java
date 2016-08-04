import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import java.util.Date;
import java.util.List;

public class DBDataTest {

    @Test
    public void dataEqualInHibernateAndJDBCMode(){

        try (JDBCManager jdbcManager = new JDBCManager();
             HibernateManager hibernateManager = new HibernateManager()) {

            List<User> usersFromHibernate = hibernateManager.getData();
            List<User> usersFromJDBC = jdbcManager.getData();

            assertEquals("Number of objects is the same", usersFromHibernate.size(), usersFromJDBC.size());

            for (int i = 0; i < usersFromHibernate.size(); i++){
                User hUser = usersFromHibernate.get(i);
                User jdbcUser = usersFromJDBC.get(i);
                assertEquals("Expected " + DateUtils.getUtc(jdbcUser.getCreatedAt()) + " Actual " + DateUtils.getUtc(hUser.getCreatedAt()),
                        jdbcUser.getCreatedAt(), hUser.getCreatedAt());
            }

        } catch (Exception e) {
        e.printStackTrace();
        }
    }

    @Test
    public void datesInsertedAndReadAreEqual(){

        try (JDBCManager jdbcManager = new JDBCManager();
             HibernateManager hibernateManager = new HibernateManager()) {

            //Prepare test user
            Date expectedDate = DateUtils.getUtc("2016-06-12 14:54:15");
            int i = 4;
            User user = new User(i, "Adam Z", expectedDate);

            //JDBC
            jdbcManager.writeUserData(user);
            List<User> usersFromJDBCUpdated = jdbcManager.getData();
            Date actualDateJDBC = usersFromJDBCUpdated.get(i - 1).getCreatedAt();
            assertTrue("JDBC Expected: " + expectedDate + " JDBC Actual: " + actualDateJDBC, expectedDate.compareTo(actualDateJDBC) == 0);

            //Hibernate
            /*hibernateManager.writeUserData(user);
            List<User> usersFromHibernate = hibernateManager.getData();
            System.out.println(usersFromHibernate.size());
            Date actualDateHibernate = usersFromHibernate.get(i - 1).getCreatedAt();*/
            //assertEquals("Hib. Expected: " + expectedDate + " Hib. Actual: " + actualDateHibernate, expectedDate, actualDateHibernate);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
