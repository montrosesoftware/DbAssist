package com.montrosesoftware.repositories;

import com.montrosesoftware.DateUtils;
import com.montrosesoftware.config.BaseTest;
import com.montrosesoftware.entities.User;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

public class DbAssistMiscellaneousTest extends BaseTest {

    @Autowired
    private UserRepo uRepo;

    @Test(expected = ConditionsAlreadyUsedException.class)
    public void conditionsAreNotReusableAfterFindCall() throws ConditionsAlreadyUsedException {
        //prepare user data:
        Date date = DateUtils.getUtc("2012-06-12 08:10:15");
        List<User> users = new ArrayList<>();
        users.add(new User(1, "User 1", date));
        users.add(new User(2, "User 2", date));
        users.forEach(uRepo::save);
        uRepo.clearPersistenceContext();

        //WHERE id >= 1 AND id <= 1
        Conditions conditions = new Conditions();
        conditions.inRangeConditions("id", 1, 1);

        //Conditions can be used only once, after calling find() or findAttribute()
        //we have to create a new instance of Conditions that we want to use
        List<User> results = uRepo.find(conditions);
        List<User> resultsAgain = uRepo.find(conditions);   //should fail
    }

    @Test
    public void conditionsNullAndNotNull() throws ConditionsAlreadyUsedException {
        //prepare user data with null date in user 2
        Date date = DateUtils.getUtc("2012-06-12 08:10:15");
        User u1 = new User(1, "Rose", date);
        User u2 = new User();
        u2.setId(2);
        u2.setName("Mont");
        uRepo.save(u1);
        uRepo.save(u2);
        uRepo.clearPersistenceContext();

        //WHERE created_at IS NULL
        Conditions condDateNull = new Conditions();
        condDateNull.isNull("createdAt");
        List<User> results = uRepo.find(condDateNull);
        assertEquals(1,results.size());
        assertEquals("Mont", results.get(0).getName());

        //WHERE created_at IS NOT NULL
        Conditions condDateNotNull = new Conditions();
        condDateNotNull.isNotNull("createdAt");
        List<User> resultsDateNotNull = uRepo.find(condDateNotNull);
        assertEquals(1,resultsDateNotNull.size());
        assertEquals("Rose", resultsDateNotNull.get(0).getName());
    }

    @Test
    public void conditionsLikeAndNotLike() throws ConditionsAlreadyUsedException {
        //prepare user data:
        Date date = DateUtils.getUtc("2012-06-12 08:10:15");
        List<User> users = new ArrayList<>();
        users.add(new User(1, "Mont", date));
        users.add(new User(2, "Rose", date));
        users.add(new User(3, "Montrose", date));
        users.forEach(uRepo::save);
        uRepo.clearPersistenceContext();

        //WHERE name NOT LIKE 'Mont%'
        Conditions conditionsA = new Conditions();
        conditionsA.like("name", "Mont%");
        List<User> resultsA = uRepo.find(conditionsA);
        assertEquals(2, resultsA.size());
        assertEquals("Mont", resultsA.get(0).getName());
        assertEquals("Montrose", resultsA.get(1).getName());

        // WHERE name LIKE 'Mont%'
        Conditions conditionsB = new Conditions();
        conditionsB.notLike("name", "Mont%");
        List<User> resultsB = uRepo.find(conditionsB);
        assertEquals(1, resultsB.size());
        assertEquals("Rose", resultsB.get(0).getName());
    }

    @Test
    public void emptyConditionsReturnAllObjects() throws ConditionsAlreadyUsedException {
        //prepare user data:
        Date date = DateUtils.getUtc("2012-06-12 08:10:15");
        List<User> users = new ArrayList<>();
        users.add(new User(1, "Mont", date));
        users.add(new User(2, "Rose", date));
        users.add(new User(3, "Montrose", date));
        users.forEach(uRepo::save);
        uRepo.clearPersistenceContext();

        // WHERE 1 = 1
        Conditions c = new Conditions();
        List<User> results = uRepo.find(c);
        assertEquals(users.size(), results.size());
    }

    @Test
    public void countUseTest() throws ConditionsAlreadyUsedException {
        //prepare user data:
        Date date = DateUtils.getUtc("2012-06-12 08:10:15");
        List<User> users = new ArrayList<>();
        users.add(new User(1, "Mont", date));
        users.add(new User(2, "Mont", date));
        users.add(new User(3, "Rose", date));
        users.forEach(uRepo::save);
        uRepo.clearPersistenceContext();

        //
        Conditions c = new Conditions();
        c.equal("name", "Mont");
        uRepo.count(c);

        //TODO finish
    }
}
