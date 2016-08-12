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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DbAssistLogicalOperationsTest extends BaseTest {

    @Autowired
    private UserRepo uRepo;

    @Autowired
    private CertificateRepo cRepo;

    @Test
    public void logicalAndConditionsTest(){
        //prepare and insert user
        Date date = DateUtils.getUtc("2016-06-12 08:10:15");
        Date dateBefore = DateUtils.getUtc("2015-02-12 08:10:15");
        Date dateAfter = DateUtils.getUtc("2018-06-12 08:10:15");

        User userToInsert = new User(1, "Joanna Spring", date);
        uRepo.save(userToInsert);
        uRepo.clearPersistenceContext();

        //conditionsA should include the user
        Conditions conditionsA = new Conditions();
        conditionsA.and(conditionsA.greaterThan("createdAt", dateBefore),
                null,
                conditionsA.lessThan("createdAt", dateAfter));
        List<User> resultsA = uRepo.find(conditionsA, null, null);
        assertEquals(1, resultsA.size());

        //conditionsB should not include the user
        Conditions conditionsB = new Conditions();
        conditionsB.and(conditionsB.greaterThan("createdAt", dateAfter),
                null,
                conditionsB.lessThan("createdAt", dateBefore));
        List<User> resultsB = uRepo.find(conditionsB, null, null);
        assertEquals(0,resultsB.size());
    }

    /*
    @Test
    public void logicalOrAndCombined(){
        //prepare data
        Date date = DateUtils.getUtc("2016-06-12 08:10:15");
        ArrayList<String> names = new ArrayList<>(
                Arrays.asList("Name 1", "Name 2", "Name 3", "Name 4", "Name 4"));
        List<User> usersToInsert = new ArrayList<>();
        for(int i=0;i<5;i++)
            usersToInsert.add(new User(i + 1, names.get(i), date));
        for(User u : usersToInsert)
            uRepo.save(u);
        uRepo.clearPersistenceContext();


        //Conditions should include users with ids = 1,2 and 5
        Conditions conditions = new Conditions();
        conditions.or(
                conditions.and(conditions.greaterThanOrEqualTo("id", 1), null, conditions.lessThanOrEqualTo("id"), 2),
                null,
                conditions.equal("name", names.get(4));
        );

        List<User> results = uRepo.find(conditions,null,null);
    }
    */

    @Test
    public void inRangeWithDates(){
        //prepare user data:
        Date date1 = DateUtils.getUtc("2012-06-12 08:10:15");
        Date date2 = DateUtils.getUtc("2015-02-12 08:10:15");
        Date date3 = DateUtils.getUtc("2018-06-12 08:10:15");
        User user1 = new User(1, "User 1", date1);
        User user2 = new User(2, "User 2", date2);
        User user3 = new User(3, "User 3", date3);
        uRepo.save(user1);
        uRepo.save(user2);
        uRepo.save(user3);
        uRepo.clearPersistenceContext();

        Conditions conditions = Conditions.inRangeConditions("createdAt", date2, date3);
        List<User> results = uRepo.find(conditions, null, null);

        assertEquals(2, results.size());
       // assertTrue(results.get(0).getCreatedAt().compareTo(date2));
        //a/ssertTrue(results.get(1).getCreatedAt().compareTo(date3));
    }

}
