package com.montrosesoftware.dbassist.repositories;

import com.montrosesoftware.dbassist.DateUtils;
import com.montrosesoftware.dbassist.config.BaseTest;
import com.montrosesoftware.dbassist.entities.User;
import com.montrosesoftware.dbassist.helpers.TestUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DbAssistAggregateTest extends BaseTest {

    @Autowired
    UserRepo uRepo;

    private static final Date ExampleDate = DateUtils.getUtc("2012-06-12 08:10:15");

    @Test
    public void countUseTest() {
        TestUtils.saveUsersData(uRepo, new ArrayList<User>(){{
            add(new User(1, "Mont", ExampleDate));
            add(new User(2, "Mont", ExampleDate));
            add(new User(3, "Rose", ExampleDate));
        }});

        // SELECT COUNT(id) FROM users WHERE name = 'Mont'
        ConditionsBuilder cA = new ConditionsBuilder();
        HierarchyCondition hcA = cA.equal("name", "Mont");
        cA.apply(hcA);
        Long countA = uRepo.count(cA);
        assertEquals(countA.longValue(), 2);

        ConditionsBuilder cB = new ConditionsBuilder();
        HierarchyCondition hcB = cB.equal("name", "Rose");
        cB.apply(hcB);

        Long countB = uRepo.count(cB);
        assertEquals(countB.longValue(), 1);

        ConditionsBuilder cC = new ConditionsBuilder();
        HierarchyCondition hcC = cC.equal("name", "Whatever");
        cC.apply(hcC);

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
        TestUtils.saveUsersData(uRepo, new ArrayList<User>(){{
            add(new User(13, "Mont", date));
            add(new User(7, "Mont", date));
            add(new User(5, "Rose", dateAnother));
        }});

        // SELECT SUM(id) FROM users WHERE created_at_utc = date
        ConditionsBuilder cA = new ConditionsBuilder();
        HierarchyCondition hcA = cA.equal("createdAtUtc", date);
        cA.apply(hcA);
        Integer sum = uRepo.sum(cA, "id");
        assertEquals(sum.intValue(), 20);

        ConditionsBuilder cB = new ConditionsBuilder();
        HierarchyCondition hcB = cB.equal("createdAtUtc", date);
        cB.apply(hcB);
        Integer min = uRepo.min(cB, "id");
        assertEquals(min.intValue(), 7);

        ConditionsBuilder cC = new ConditionsBuilder();
        HierarchyCondition hcC = cC.equal("createdAtUtc", date);
        cC.apply(hcC);
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
        TestUtils.saveUsersData(uRepo, new ArrayList<User>(){{
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
        TestUtils.saveUsersData(uRepo, new ArrayList<User>(){{
            add(new User(1, "Mont", ExampleDate));
            add(new User(2, "Mont", ExampleDate));
            add(new User(3, "Rose", ExampleDate));
        }});

        // SELECT COUNT(id) FROM users WHERE name = 'Mont'
        ConditionsBuilder c = new ConditionsBuilder();
        HierarchyCondition hc = c.equal("name", "Mont");
        c.apply(hc);
        Long count = uRepo.count(c);
        assertEquals(count.longValue(), 2);

        Long countAgain = uRepo.count(c);   //should fail and throw RuntimeException
    }

    @Test
    public void avgAggregate(){
        TestUtils.saveUsersData(uRepo, new ArrayList<User>(){{
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
    public void leastAndGreatestAggregateDate(){
        Date date1 = DateUtils.getUtc("2015-06-12 08:10:15");
        Date date2 = DateUtils.getUtc("2011-06-12 09:10:15");
        Date date3 = DateUtils.getUtc("2025-06-12 10:10:15");
        TestUtils.saveUsersData(uRepo, new ArrayList<User>(){{
            add(new User(1, "BB", date1));
            add(new User(2, "AA", date2));
            add(new User(3, "CC", date3));
        }});

        Date dateMinRead = uRepo.least(new ConditionsBuilder(), "createdAtUtc");
        assertTrue(dateMinRead.compareTo(date2) == 0);

        Date dateMaxRead = uRepo.greatest(new ConditionsBuilder(), "createdAtUtc");
        assertTrue(dateMaxRead.compareTo(date3) == 0);

        String nameMinRead = uRepo.least(new ConditionsBuilder(), "name");
        assertEquals(nameMinRead, "AA");

        String nameMaxRead = uRepo.greatest(new ConditionsBuilder(), "name");
        assertEquals(nameMaxRead, "CC");
    }
}
