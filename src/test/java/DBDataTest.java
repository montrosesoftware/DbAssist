import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.List;

public class DBDataTest {

    HibernateManager hibernateManager;
    //JDBCManager jdbcManager;

    @Before
    public void prepareManagers(){
        hibernateManager = new HibernateManager();
        //jdbcManager = new JDBCManager();
    }

    @Test
    public void dataEqualInHibernateAndJDBCMode(){

        try (JDBCManager jdbcManager = new JDBCManager()) {

            List<User> usersFromHibernate = hibernateManager.getData();
            List<User> usersFromJDBC = jdbcManager.getData();

            assertEquals("Number of objects is the same", usersFromHibernate.size(), usersFromJDBC.size());

            for (int i = 0; i < usersFromHibernate.size(); i++){
                //assertEquals("Element of id = " + i + " has to be equal in both sources.", usersFromHibernate.get(i).getCreatedAt(), usersFromJDBC.get(i).getCreatedAt());
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
    public void datesInsertedAndReadAreNotEqual(){

        try (JDBCManager jdbcManager = new JDBCManager()) {
            Date expectedDate = DateUtils.getUtc("2016-06-12 14:54:15");
            int i = 5;
            User user = new User(i, "Adam Z", expectedDate);
            jdbcManager.writeUserData(user);
            List<User> usersFromJDBCUpdated = jdbcManager.getData();
            Date actualDate = usersFromJDBCUpdated.get(i - 1).getCreatedAt();
            assertTrue(expectedDate.compareTo(actualDate) == 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
