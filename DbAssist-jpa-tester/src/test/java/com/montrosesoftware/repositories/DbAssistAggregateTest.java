package com.montrosesoftware.repositories;

import com.montrosesoftware.DateUtils;
import com.montrosesoftware.config.BaseTest;
import com.montrosesoftware.entities.User;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DbAssistAggregateTest extends BaseTest {

    @Autowired
    UserRepo uRepo;

    @Test
    public void countUseTest() {
        //prepare user data:
        Date date = DateUtils.getUtc("2012-06-12 08:10:15");
        List<User> users = new ArrayList<>();
        users.add(new User(1, "Mont", date));
        users.add(new User(2, "Mont", date));
        users.add(new User(3, "Rose", date));
        users.forEach(uRepo::save);
        uRepo.clearPersistenceContext();

        // SELECT COUNT(id) FROM users WHERE name = 'Mont'
        Conditions cA = new Conditions();
        cA.equal("name", "Mont");
        Long countA = uRepo.count(cA);
        assertEquals(countA.longValue(), 2);

        Conditions cB = new Conditions();
        cB.equal("name", "Rose");
        Long countB = uRepo.count(cB);
        assertEquals(countB.longValue(), 1);

        Conditions cC = new Conditions();
        cC.equal("name", "Whatever");
        Long countC = uRepo.count(cC);
        assertEquals(countC.longValue(), 0);
    }

    @Test
    public void countOnEmptyTable(){
        Conditions c = new Conditions();
        Long count = uRepo.count(c);
        assertEquals(count.longValue(), 0);
    }

    @Test
    public void sumMinMaxUseTest(){
        //prepare user data:
        Date date = DateUtils.getUtc("2012-06-12 08:10:15");
        Date dateAnother = DateUtils.getUtc("2020-06-12 15:10:15");
        List<User> users = new ArrayList<>();
        users.add(new User(13, "Mont", date));
        users.add(new User(7, "Mont", date));
        users.add(new User(5, "Rose", dateAnother));
        users.forEach(uRepo::save);
        uRepo.clearPersistenceContext();

        // SELECT SUM(id) FROM users WHERE created_at = date
        Conditions cA = new Conditions();
        cA.equal("createdAt", date);
        Integer sum = uRepo.sum(cA, "id");
        assertEquals(sum.intValue(), 20);

        Conditions cB = new Conditions();
        cB.equal("createdAt", date);
        Integer min = uRepo.min(cB, "id");
        assertEquals(min.intValue(), 7);

        Conditions cC = new Conditions();
        cC.equal("createdAt", date);
        Integer max = uRepo.max(cC, "id");
        assertEquals(max.intValue(), 13);
    }

    @Test
    public void minOnEmptyTable(){
        Conditions c = new Conditions();
        Integer min = uRepo.min(c, "id");
        assertTrue(min == null);
    }

    @Test
    public void conditionsAreNotReusableAfterCallingAggregate(){
        //prepare user data:
        Date date = DateUtils.getUtc("2012-06-12 08:10:15");
        List<User> users = new ArrayList<>();
        users.add(new User(1, "Mont", date));
        users.add(new User(2, "Mont", date));
        users.add(new User(3, "Rose", date));
        users.forEach(uRepo::save);
        uRepo.clearPersistenceContext();

        // SELECT COUNT(id) FROM users WHERE name = 'Mont'
        Conditions c = new Conditions();
        c.equal("name", "Mont");
        Long count = uRepo.count(c);
        assertEquals(count.longValue(), 2);

        Long countAgain = uRepo.count(c);
        assertTrue(countAgain == null);
    }
}
