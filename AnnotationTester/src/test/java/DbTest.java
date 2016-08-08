import com.montrosesoftware.DateUtils;
import com.montrosesoftware.HibernateManagerAnnot;
import com.montrosesoftware.UserAnnot;
import org.junit.Test;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class DbTest {

    @Test
    public void dataReadInHibernateAndJDBCReadModeIsEqualAnnot(){
        try ( HibernateManagerAnnot hibernateManager = new HibernateManagerAnnot()) {

            String expDateString = "2016-06-12 14:54:15";
            Date expectedDate = DateUtils.getUtc(expDateString);
            int id = 1;
            UserAnnot user = new UserAnnot(id, "Adam Annot", expectedDate);

            hibernateManager.writeUserDataByPlainSQL(user);
            List<UserAnnot> usersFromHibernate = hibernateManager.getData();

            hibernateManager.rollbackTransaction();

            assertEquals(1, usersFromHibernate.size());
            UserAnnot hibernateReadUser = usersFromHibernate.get(0);

            assertEquals("Names are not the same", user.getName(), hibernateReadUser.getName());
            assertEquals("Dates are not the same", user.getCreatedAt(), hibernateReadUser.getCreatedAt());

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    @Test
    public void dataInsertedAndReadIsEqualAnnot(){

        try ( HibernateManagerAnnot hibernateManager = new HibernateManagerAnnot()) {

            //Prepare test user
            Date expectedDate = DateUtils.getUtc("2016-06-12 14:54:15");
            int id = 1;
            UserAnnot user = new UserAnnot(id, "Adam Z", expectedDate);

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
