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

public class DbAssistLogicalOperationsTest extends BaseTest {

    @Autowired
    private UserRepo uRepo;

    @Test
    public void logicalAndConditionsTest() throws ConditionsAlreadyUsedException {
        //prepare and insert user
        Date date = DateUtils.getUtc("2016-06-12 08:10:15");
        Date dateBefore = DateUtils.getUtc("2015-02-12 08:10:15");
        Date dateAfter = DateUtils.getUtc("2018-06-12 08:10:15");

        User userToInsert = new User(1, "Joanna Spring", date);
        uRepo.save(userToInsert);
        uRepo.clearPersistenceContext();

        // WHERE created_at > dateBefore AND created_at < dateAfter
        Conditions cA = new Conditions();
        cA.and(
                cA.greaterThan("createdAt", dateBefore),
                cA.lessThan("createdAt", dateAfter)
        );
        List<User> resultsA = uRepo.find(cA);
        assertEquals(1, resultsA.size());

        // WHERE created_at > dateAfter AND created_at < dateBefore
        Conditions cB = new Conditions();
        cB.and(
                cB.greaterThan("createdAt", dateAfter),
                cB.lessThan("createdAt", dateBefore)
        );
        List<User> resultsB = uRepo.find(cB);
        assertEquals(0,resultsB.size());
    }

    @Test
    public void logicalOrAndAndCombined() throws ConditionsAlreadyUsedException {
        //prepare data
        Date date = DateUtils.getUtc("2016-06-12 08:10:15");
        ArrayList<String> names = new ArrayList<>(Arrays.asList("A", "A", "B", "C", "C"));
        List<User> users = new ArrayList<>();
        for(int i=0; i<5; i++)
            users.add(new User(i + 1, names.get(i), date));
        users.forEach(uRepo::save);
        uRepo.clearPersistenceContext();

        // WHERE id >= 1 AND id <= 2 OR name = "C"
        Conditions c = new Conditions();
        c.or(
                c.and(c.greaterThanOrEqualTo("id", 1), null, c.lessThanOrEqualTo("id", 2)),
                c.equal("name", names.get(4))
        );

        List<User> results = uRepo.find(c);
        assertEquals(4, results.size());
        results.forEach(user -> {if (user.getId() == 3) fail("User id=3 should not be included according to the c");});
    }

    private static Date addMinutes(Date date, int minutes){
        final long OneMinuteInMs = 60000;
        long currentTimeInMs = date.getTime();
        Date newDate = new Date(currentTimeInMs + (minutes * OneMinuteInMs));
        return newDate;
    }

    @Test
    public void inRangeWithDatesAndIds() throws ConditionsAlreadyUsedException {
        //prepare user data:
        Date date1 = DateUtils.getUtc("2012-06-12 08:10:15");
        Date date2 = addMinutes(date1, 10);
        Date date3 = addMinutes(date1, 20);
        Date date4 = addMinutes(date1, 30);
        List<User> users = new ArrayList<>();
        users.add(new User(1, "User 1", date1));
        users.add(new User(2, "User 2", date2));
        users.add(new User(3, "User 3", date3));
        users.add(new User(4, "User 4", date4));
        users.forEach(uRepo::save);
        uRepo.clearPersistenceContext();

        // WHERE created_at >= ? AND created_at <= ?
        Conditions conditionsA = new Conditions();
        conditionsA.inRangeConditions("createdAt", date2, addMinutes(date3, 1));
        List<User> resultsA = uRepo.find(conditionsA);
        assertEquals(2, resultsA.size());
        assertTrue(resultsA.get(0).getCreatedAt().compareTo(date2) == 0);
        assertTrue(resultsA.get(1).getCreatedAt().compareTo(date3) == 0);

        // WHERE (created_at >= ? AND created_at <= ?) AND (id >= ? AND id <= ?)
        Conditions conditionsB = new Conditions();
        conditionsB.inRangeConditions("createdAt", date2, addMinutes(date3, 1));
        conditionsB.inRangeConditions("id", 3, 4);
        List<User> resultsB = uRepo.find(conditionsB);
        assertEquals(1, resultsB.size());
        assertEquals(3, resultsB.get(0).getId());

        //incorrect boundaries
        Conditions conditionsC = new Conditions();
        conditionsC.inRangeConditions("createdAt", date3, date2);
        List<User> resultsC = uRepo.find(conditionsC);
        assertEquals(0,resultsC.size());
    }

    @Test(expected = ConditionsAlreadyUsedException.class)
    public void conditionsAreNotReusableAfterFindCall() throws ConditionsAlreadyUsedException {
        //prepare user data:
        Date date1 = DateUtils.getUtc("2012-06-12 08:10:15");
        Date date2 = addMinutes(date1, 10);
        List<User> users = new ArrayList<>();
        users.add(new User(1, "User 1", date1));
        users.add(new User(2, "User 2", date2));
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
}
