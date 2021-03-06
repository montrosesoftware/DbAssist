package com.montrosesoftware.dbassist.repositories;

import com.montrosesoftware.dbassist.DateUtils;
import com.montrosesoftware.dbassist.config.BaseTest;
import com.montrosesoftware.dbassist.entities.User;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.criteria.JoinType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static com.montrosesoftware.dbassist.helpers.TestUtils.addMinutes;
import static com.montrosesoftware.dbassist.helpers.TestUtils.prepareAndSaveExampleDataToDb;
import static com.montrosesoftware.dbassist.helpers.TestUtils.saveUsersData;
import static com.montrosesoftware.dbassist.repositories.ConditionsBuilder.or;
import static org.junit.Assert.*;
import static com.montrosesoftware.dbassist.repositories.ConditionsBuilder.and;

public class DbAssistLogicalOperationsTest extends BaseTest {

    @Autowired
    private UserRepo uRepo;

    private static final Date ExampleDate = DateUtils.getUtc("2012-06-12 08:10:15");

    @Test
    public void logicalAndConditionsBuilderTest(){
        //prepare and insert user
        Date date = DateUtils.getUtc("2016-06-12 08:10:15");
        Date dateBefore = DateUtils.getUtc("2015-02-12 08:10:15");
        Date dateAfter = DateUtils.getUtc("2018-06-12 08:10:15");
        User userToInsert = new User(1, "Joanna Spring", date);
        uRepo.save(userToInsert);
        uRepo.clearPersistenceContext();

        // WHERE created_at_utc > dateBefore AND created_at < dateAfter
        ConditionsBuilder cA = new ConditionsBuilder();
        HierarchyCondition hcA = and(cA.greaterThan("createdAtUtc", dateBefore), cA.lessThan("createdAtUtc", dateAfter));
        cA.apply(hcA);
        List<User> resultsA = uRepo.find(cA);
        assertEquals(1, resultsA.size());

        // WHERE created_at_utc > dateAfter AND created_at_utc < dateBefore
        ConditionsBuilder cB = new ConditionsBuilder();
        HierarchyCondition hcB = and(
                cB.greaterThan("createdAtUtc", dateAfter),
                cB.lessThan("createdAtUtc", dateBefore)
        );
        cB.apply(hcB);
        List<User> resultsB = uRepo.find(cB);
        assertEquals(0,resultsB.size());
    }

    @Test
    public void logicalOrAndAndCombined(){
        //prepare data
        ArrayList<String> names = new ArrayList<>(Arrays.asList("A", "A", "B", "C", "C"));
        List<User> users = new ArrayList<>();
        for(int i=0; i<5; i++)
            users.add(new User(i + 1, names.get(i), ExampleDate));
        saveUsersData(uRepo, users);

        // WHERE id >= 1 AND id <= 2 OR name = "C"
        ConditionsBuilder c = new ConditionsBuilder();
        HierarchyCondition hc = or(
                and(c.greaterThanOrEqualTo("id", 1), c.lessThanOrEqualTo("id", 2)),
                c.equal("name", names.get(4))
        );
        c.apply(hc);

        List<User> results = uRepo.find(c);
        assertEquals(4, results.size());
        results.forEach(user -> {if (user.getId() == 3) fail("User id = 3 should not be included according to the conditions");});
    }

    @Test
    public void inRangeWithDatesAndIds(){
        Date date1 = ExampleDate;
        Date date2 = addMinutes(ExampleDate, 10);
        Date date3 = addMinutes(ExampleDate, 20);
        Date date4 = addMinutes(ExampleDate, 30);
        saveUsersData(uRepo, new ArrayList<User>(){{
            add(new User(1, "User 1", date1));
            add(new User(2, "User 2", date2));
            add(new User(3, "User 3", date3));
            add(new User(4, "User 4", date4));
        }});

        // WHERE created_at_utc >= ? AND created_at_utc <= ?
        ConditionsBuilder conditionsA = new ConditionsBuilder();
        HierarchyCondition hcA = conditionsA.inRangeCondition("createdAtUtc", date2, addMinutes(date3, 1));
        conditionsA.apply(hcA);
        List<User> resultsA = uRepo.find(conditionsA);
        assertEquals(2, resultsA.size());
        assertTrue(resultsA.get(0).getCreatedAtUtc().compareTo(date2) == 0);
        assertTrue(resultsA.get(1).getCreatedAtUtc().compareTo(date3) == 0);

        //we cannot reuse the previous conditions (find(...) was already executed)
        // WHERE (created_at_utc >= ? AND created_at_utc <= ?) AND (id >= ? AND id <= ?)
        ConditionsBuilder conditionsB = new ConditionsBuilder();
        HierarchyCondition hcB = and(
                conditionsB.inRangeCondition("createdAtUtc", date2, addMinutes(date3, 1)),
                conditionsB.inRangeCondition("id", 3, 4));
        conditionsB.apply(hcB);
        List<User> resultsB = uRepo.find(conditionsB);
        assertEquals(1, resultsB.size());
        assertEquals(3, resultsB.get(0).getId());

        //incorrect boundaries
        ConditionsBuilder conditionsC = new ConditionsBuilder();
        HierarchyCondition hcC = conditionsC.inRangeCondition("createdAtUtc", date3, date2);
        conditionsC.apply(hcC);
        List<User> resultsC = uRepo.find(conditionsC);
        assertEquals(0,resultsC.size());

        // WHERE (id > 1 AND id < 3)
        ConditionsBuilder conditionsD = new ConditionsBuilder();
        HierarchyCondition hcD = conditionsD.inRangeExclusiveCondition("id",1,3);
        conditionsD.apply(hcD);
        List<User> resultsD = uRepo.find(conditionsD);
        assertEquals(1, resultsD.size());
        assertEquals(2, resultsD.get(0).getId());
    }

    @Test
    public void joinTestMultipleEntitiesWithListOfConditionsOr() {
        prepareAndSaveExampleDataToDb(uRepo);

        //handle joins
        ConditionsBuilder builderUsers = new ConditionsBuilder();
        ConditionsBuilder builderCertificates = builderUsers.join("certificates", JoinType.LEFT);
        ConditionsBuilder builderProviders = builderCertificates.join("provider", JoinType.LEFT);
        ConditionsBuilder builderCountries = builderProviders.join("country", JoinType.LEFT);

        HierarchyCondition c1 = builderUsers.lessThan("id", 15);
        HierarchyCondition c2 = builderCertificates.lessThan("id", 13);
        HierarchyCondition c3 = builderProviders.lessThan("id", 9);
        HierarchyCondition c4 = builderCountries.equal("name", "Provider 1");

        HierarchyCondition hc = or(Arrays.asList(c1, c2, c3, c4));

        builderUsers.apply(hc);

        // ... WHERE c1 OR c2 OR c3 OR c4
        List<User> usersReadMultipleJoin = uRepo.find(builderUsers);
        assertEquals(usersReadMultipleJoin.size(), 3);
    }

    @Test
    public void joinTestMultipleEntitiesWithListOfConditionsAnd() {
        prepareAndSaveExampleDataToDb(uRepo);

        //handle joins
        ConditionsBuilder builderUsers = new ConditionsBuilder();
        ConditionsBuilder builderCertificates = builderUsers.join("certificates", JoinType.LEFT);
        ConditionsBuilder builderProviders = builderCertificates.join("provider", JoinType.LEFT);
        ConditionsBuilder builderCountries = builderProviders.join("country", JoinType.LEFT);

        HierarchyCondition c1 = builderUsers.lessThan("id", 12);
        HierarchyCondition c2 = builderCertificates.lessThan("id", 13);
        HierarchyCondition c3 = builderProviders.lessThan("id", 9);
        HierarchyCondition c4 = builderCountries.equal("name", "Provider 1");

        HierarchyCondition hc = and(Arrays.asList(c1, c2, c3, c4));

        builderUsers.apply(hc);

        // ... WHERE c1 AND c2 AND c3 AND c4
        List<User> usersReadMultipleJoin = uRepo.find(builderUsers);
        assertEquals(usersReadMultipleJoin.size(), 0);
        //TODO change conditions
    }

    @Test
    public void hierarchyConditionUseTest(){
                //handle joins
        ConditionsBuilder builderUsers = new ConditionsBuilder();
        ConditionsBuilder builderCertificates = builderUsers.join("certificates", JoinType.LEFT);
        ConditionsBuilder builderProviders = builderCertificates.join("provider", JoinType.LEFT);
        ConditionsBuilder builderCountries = builderProviders.join("country", JoinType.LEFT);

        HierarchyCondition c1 = builderUsers.lessThan("id", 15);
        HierarchyCondition c2 = builderCertificates.lessThan("id", 13);
        HierarchyCondition c3 = builderProviders.lessThan("id", 9);
        HierarchyCondition c4 = builderCountries.equal("name", "USA");

        HierarchyCondition hc = and(or(and(c1, c2),c3),c4);
        builderUsers.apply(hc);

        // ... WHERE (user.id < ? AND certificate.id < ? OR provider.id < ?) AND country.name = ?
        List<User> results = uRepo.find(builderUsers);
        assertEquals(results.size(), 0);
    }
}
