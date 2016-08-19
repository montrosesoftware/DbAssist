package com.montrosesoftware.repositories;

import com.montrosesoftware.DateUtils;
import com.montrosesoftware.config.BaseTest;
import com.montrosesoftware.entities.User;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DbAssistAggregateTest extends BaseTest {

    @Autowired
    UserRepo uRepo;

    private void saveUsersData(List<User> usersToSave){
        usersToSave.forEach(uRepo::save);
        uRepo.clearPersistenceContext();
    }

    private static final Date ExampleDate = DateUtils.getUtc("2012-06-12 08:10:15");

    @Test
    public void countUseTest() {
        saveUsersData(new ArrayList<User>(){{
            add(new User(1, "Mont", ExampleDate));
            add(new User(2, "Mont", ExampleDate));
            add(new User(3, "Rose", ExampleDate));
        }});

        // SELECT COUNT(id) FROM users WHERE name = 'Mont'
        ConditionsBuilder cA = new ConditionsBuilder();
        cA.equal("name", "Mont");
        Long countA = uRepo.count(cA);
        assertEquals(countA.longValue(), 2);

        ConditionsBuilder cB = new ConditionsBuilder();
        cB.equal("name", "Rose");
        Long countB = uRepo.count(cB);
        assertEquals(countB.longValue(), 1);

        ConditionsBuilder cC = new ConditionsBuilder();
        cC.equal("name", "Whatever");
        Long countC = uRepo.count(cC);
        assertEquals(countC.longValue(), 0);
    }

    @Test
    public void countOnEmptyTable(){
        ConditionsBuilder c = new ConditionsBuilder();
        Long count = uRepo.count(c);
        assertEquals(count.longValue(), 0);
    }

    @Test
    public void sumMinMaxUseTest(){
        Date date = DateUtils.getUtc("2012-06-12 08:10:15");
        Date dateAnother = DateUtils.getUtc("2020-06-12 15:10:15");
        saveUsersData(new ArrayList<User>(){{
            add(new User(13, "Mont", date));
            add(new User(7, "Mont", date));
            add(new User(5, "Rose", dateAnother));
        }});

        // SELECT SUM(id) FROM users WHERE created_at = date
        ConditionsBuilder cA = new ConditionsBuilder();
        cA.equal("createdAt", date);
        Integer sum = uRepo.sum(cA, "id");
        assertEquals(sum.intValue(), 20);

        ConditionsBuilder cB = new ConditionsBuilder();
        cB.equal("createdAt", date);
        Integer min = uRepo.min(cB, "id");
        assertEquals(min.intValue(), 7);

        ConditionsBuilder cC = new ConditionsBuilder();
        cC.equal("createdAt", date);
        Integer max = uRepo.max(cC, "id");
        assertEquals(max.intValue(), 13);
    }

    @Test
    public void minOnEmptyTable(){
        Integer min = uRepo.min(new ConditionsBuilder(), "id");
        assertTrue(min == null);
    }

    private static final double Delta = 1e-15;

    @Test
    public void sumNormalAsLongAndAsDouble(){
        saveUsersData(new ArrayList<User>(){{
            add(new User(1, "Mont", ExampleDate, 14.5, "worker"));
            add(new User(2, "Mont", ExampleDate, 10.1, "worker"));
            add(new User(3, "Rose", ExampleDate, 1.5, "worker"));
        }});

        //id is of type: int
        Integer sumIds = uRepo.sum(new ConditionsBuilder(), "id");
        assertEquals(sumIds.intValue(), 6);

        Long sumIdsLong = uRepo.sumAsLong(new ConditionsBuilder(), "id");
        assertEquals(sumIdsLong.longValue(), 6);

        Double sumIdsDouble = uRepo.sumAsDouble(new ConditionsBuilder(), "id");
        assertEquals(sumIdsDouble, 6.0, Delta);

        //salary is of type: double
        Double sumSalaries = uRepo.sum(new ConditionsBuilder(), "salary");
        assertEquals(sumSalaries, 14.5 + 10.1 + 1.5, Delta);

        Long sumSalariesAsLong = uRepo.sumAsLong(new ConditionsBuilder(), "salary");
        assertEquals(sumSalariesAsLong.longValue(), 26);

        Double sumSalariesAsDouble = uRepo.sumAsDouble(new ConditionsBuilder(), "salary");
        assertEquals(sumSalariesAsDouble, 14.5 + 10.1 + 1.5, Delta);
    }

    @Test(expected = RuntimeException.class)
    public void conditionsAreNotReusableAfterCallingAggregate(){
        saveUsersData(new ArrayList<User>(){{
            add(new User(1, "Mont", ExampleDate));
            add(new User(2, "Mont", ExampleDate));
            add(new User(3, "Rose", ExampleDate));
        }});

        // SELECT COUNT(id) FROM users WHERE name = 'Mont'
        ConditionsBuilder c = new ConditionsBuilder();
        c.equal("name", "Mont");
        Long count = uRepo.count(c);
        assertEquals(count.longValue(), 2);

        Long countAgain = uRepo.count(c);   //should fail and throw RuntimeException
    }

    @Test
    public void avgAggregate(){
        saveUsersData(new ArrayList<User>(){{
            add(new User(1, "Mont", ExampleDate));
            add(new User(2, "Rose", ExampleDate));
            add(new User(3, "Montrose", ExampleDate));
        }});

        Double avg = uRepo.avg(new ConditionsBuilder(), "id");
        assertEquals(avg, 2.0, Delta);
    }

    @Test
    public void avgOnEmptyTable(){
        Double avg = uRepo.avg(new ConditionsBuilder(), "id");
        assertTrue(avg == null);
    }

    @Test
    public void leastAggregateDate(){
        Date date1 = DateUtils.getUtc("2015-06-12 08:10:15");
        Date date2 = DateUtils.getUtc("2011-06-12 09:10:15");
        Date date3 = DateUtils.getUtc("2025-06-12 10:10:15");
        saveUsersData(new ArrayList<User>(){{
            add(new User(1, "BB", date1));
            add(new User(2, "AA", date2));
            add(new User(3, "CC", date3));
        }});

        Date dateMinRead = uRepo.least(new ConditionsBuilder(), "createdAt");
        assertTrue(dateMinRead.compareTo(date2) == 0);

        Date dateMaxRead = uRepo.greatest(new ConditionsBuilder(), "createdAt");
        assertTrue(dateMaxRead.compareTo(date3) == 0);

        String nameMinRead = uRepo.least(new ConditionsBuilder(), "name");
        assertEquals(nameMinRead, "AA");

        String nameMaxRead = uRepo.greatest(new ConditionsBuilder(), "name");
        assertEquals(nameMaxRead, "CC");
    }
}
