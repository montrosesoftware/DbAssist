import com.montrosesoftware.DateUtils;
import com.montrosesoftware.HibernateManager;
import com.montrosesoftware.User;
import org.junit.Test;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DateShiftTest {

    public DateShiftTest(){}

    private User getExampleUserData() {
        //prepare example user data
        Date expectedDatetime = DateUtils.getUtc("2016-06-12 14:54:15");
        Date expectedDateOnly = DateUtils.getUtc("2016-03-02", true);
        Timestamp expectedTimestamp = new Timestamp(expectedDatetime.getTime());
        User user = new User(1, "Adam Spring", expectedDatetime, expectedTimestamp, expectedDateOnly);
        return user;
    }

    private void assertTimeInDatesNotShifted(User userExpected, User userActual) {
        assertNotNull(userActual);
        assertEquals("Names are not the same", userExpected.getName(), userActual.getName());
        assertEquals("Datetimes are not the same", userExpected.getCreatedAtUtc(), userActual.getCreatedAtUtc());
        assertEquals("Timestamps are not the same", userExpected.getUpdatedAtUtc(), userActual.getUpdatedAtUtc());
        assertEquals("Dates are not the same", userExpected.getLastLoggedAtUtc(), userActual.getLastLoggedAtUtc());
    }

    @Test
    public void dataReadInHibernateAndJDBCReadModeIsEqual(){
        try ( HibernateManager hibernateManager = new HibernateManager()) {
            User user = getExampleUserData();

            hibernateManager.writeUserDataByPlainSQL(user);
            List<User> usersFromHibernate = hibernateManager.getData();
            hibernateManager.rollbackTransaction();

            assertEquals(1, usersFromHibernate.size());
            User userRead = usersFromHibernate.get(0);

            assertTimeInDatesNotShifted(user, userRead);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    @Test
    public void dataInsertedAndReadIsEqual(){

        try ( HibernateManager hibernateManager = new HibernateManager()) {

            //Prepare test user
            User user = getExampleUserData();

            //Plain SQL
            hibernateManager.writeUserDataByPlainSQL(user);

            //Hibernate
            user.setId(user.getId() + 1);
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
