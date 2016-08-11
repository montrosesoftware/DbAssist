import com.montrosesoftware.DateUtils;
import com.montrosesoftware.HibernateManager;
import com.montrosesoftware.User;
import org.junit.Test;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class DBDataTest {

    public DBDataTest(){}

    @Test
    public void dataReadInHibernateAndJDBCReadModeIsEqual(){
        try ( HibernateManager hibernateManager = new HibernateManager()) {

            //insert one user
            String expDateString = "2016-06-12 14:54:15";
            Date expectedDate = DateUtils.getUtc(expDateString);
            int id = 1;
            User user = new User(id, "Adam Z", expectedDate);

            hibernateManager.writeUserDataByPlainSQL(user);
            List<User> usersFromHibernate = hibernateManager.getData();
            hibernateManager.rollbackTransaction();

            assertEquals(1, usersFromHibernate.size());
            User hibernateReadUser = usersFromHibernate.get(0);

            assertEquals("Names are not the same", user.getName(), hibernateReadUser.getName());
            assertEquals("Dates are not the same", user.getCreatedAt(), hibernateReadUser.getCreatedAt());

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    @Test
    public void dataInsertedAndReadIsEqual(){

        try ( HibernateManager hibernateManager = new HibernateManager()) {

            //Prepare test user
            Date expectedDate = DateUtils.getUtc("2016-06-12 14:54:15");
            int id = 1;
            User user = new User(id, "Adam Z", expectedDate);

            //Plain SQL
            hibernateManager.writeUserDataByPlainSQL(user);

            //Hibernate
            user.setId(id + 1);
            hibernateManager.writeUserData(user);

            List<Object[]> userObjects = hibernateManager.getDataByPlainSQL();
            hibernateManager.rollbackTransaction();
            assertEquals(2, userObjects.size());

            int columnsAmount = userObjects.get(0).length;
            for (int i = 0; i < columnsAmount; i++) {
                Object[] first = userObjects.get(0);
                Object[] second = userObjects.get(1);
                assertEquals(first[i], second[i]);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }
}
