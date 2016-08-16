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
        long countA = uRepo.count(cA);
        assertEquals(countA, 2);

        Conditions cB = new Conditions();
        cB.equal("name", "Rose");
        long countB = uRepo.count(cB);
        assertEquals(countB, 1);

        Conditions cC = new Conditions();
        cC.equal("name", "Whatever");
        long countC = uRepo.count(cC);
        assertEquals(countC, 0);
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
        int sum = uRepo.sum(cA, "id", Integer.class);
        assertEquals(sum, 20);

        Conditions cB = new Conditions();
        cB.equal("createdAt", date);
        int min = uRepo.min(cB, "id", Integer.class);
        assertEquals(min, 7);

        Conditions cC = new Conditions();
        cC.equal("createdAt", date);
        int max = uRepo.max(cC, "id", Integer.class);
        assertEquals(max, 13);
    }
}
