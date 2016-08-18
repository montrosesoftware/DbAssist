package com.montrosesoftware.repositories;

import com.montrosesoftware.DateUtils;
import com.montrosesoftware.config.BaseTest;
import com.montrosesoftware.entities.User;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.Tuple;
import javax.persistence.criteria.Selection;
import java.util.*;

import static org.junit.Assert.*;

public class DbAssistMiscellaneousTest extends BaseTest {

    @Autowired
    private UserRepo uRepo;

    private void saveUsersData(List<User> usersToSave){
        usersToSave.forEach(uRepo::save);
        uRepo.clearPersistenceContext();
    }

    private <T> boolean collectionsAreEqual(Collection<T> a, Collection<T> b){
        return a.containsAll(b) && b.containsAll(a);
    }

    public static final Date ExampleDate = DateUtils.getUtc("2012-06-12 08:10:15");

    @Test
    public void conditionsAreNotReusableAfterCallingFind(){
        saveUsersData(new ArrayList<User>(){{
            add(new User(1, "User 1", ExampleDate));
            add(new User(2, "User 2", ExampleDate));
        }});

        //WHERE id >= 1 AND id <= 1
        Conditions conditions = new Conditions();
        conditions.inRangeConditions("id", 1, 1);

        //Conditions can be used only once, after calling find() or findAttribute()
        //we have to create a new instance of Conditions that we want to use
        List<User> results = uRepo.find(conditions);
        List<User> resultsAgain = uRepo.find(conditions);   //should fail (return null pointer)
        assertNotNull(results);
        assertNull(resultsAgain);
    }

    @Test
    public void findAttributeUse(){
        Date date = DateUtils.getUtc("2012-06-12 08:10:15");
        Date dateAnother = DateUtils.getUtc("2000-03-03 11:10:15");
        saveUsersData(new ArrayList<User>(){{
            add(new User(1, "A", date));
            add(new User(2, "B", dateAnother));
            add(new User(3, "C", date));
        }});

        Conditions c = new Conditions();
        c.equal("createdAt", date);
        List<String> namesRead = uRepo.findAttribute("name", c);

        assertEquals(namesRead.size(),2);
        List<String> namesExpected = new ArrayList<>(Arrays.asList("A", "C"));
        assertTrue(collectionsAreEqual(namesRead, namesExpected));
    }

    @Test
    public void findAttributesUse(){
        saveUsersData(new ArrayList<User>(){{
            add(new User(1, "Mont", ExampleDate));
            add(new User(2, "Rose", ExampleDate));
            add(new User(3, "Montrose", ExampleDate));
        }});

        AbstractRepository.SelectionList<User> selectionList = (criteriaBuilder, root) -> {
            List<Selection<?>> list = new ArrayList<>();
            list.add(root.get("id"));
            list.add(root.get("name"));
            return  list;
        };

        Conditions c = new Conditions();
        c.inRangeConditions("id",1,2);
        List<Tuple> tuples = uRepo.findAttributes(selectionList, c);
        List<Integer> idsRead = new ArrayList<>();
        List<String> namesRead = new ArrayList<>();
        tuples.forEach((tuple -> {
            idsRead.add((Integer) tuple.get(0));
            namesRead.add((String) tuple.get(1));
        }));

        List<String> namesExpected = new ArrayList<>(Arrays.asList("Mont", "Rose"));
        List<Integer> idsExpected = new ArrayList<>(Arrays.asList(1, 2));
        assertTrue(collectionsAreEqual(namesRead, namesExpected));
        assertTrue(collectionsAreEqual(idsRead, idsExpected));
    }

    @Test
    public void conditionsNullAndNotNull() {
        //prepare user data with null date in user 2
        User u1 = new User(1, "Rose", ExampleDate);
        User u2 = new User();
        u2.setId(2);
        u2.setName("Mont");
        uRepo.save(u1);
        uRepo.save(u2);
        uRepo.clearPersistenceContext();

        //WHERE created_at IS NULL
        Conditions condDateNull = new Conditions();
        condDateNull.isNull("createdAt");
        List<String> resultsDateNull = uRepo.findAttribute("name", condDateNull);
        assertEquals(1, resultsDateNull.size());
        assertEquals("Mont", resultsDateNull.get(0));

        //WHERE created_at IS NOT NULL
        Conditions condDateNotNull = new Conditions();
        condDateNotNull.isNotNull("createdAt");
        List<String> resultsDateNotNull = uRepo.findAttribute("name", condDateNotNull);
        assertEquals(1, resultsDateNotNull.size());
        assertEquals("Rose", resultsDateNotNull.get(0));
    }

    @Test
    public void conditionsLikeAndNotLike() {
        saveUsersData(new ArrayList<User>(){{
            add(new User(1, "Mont", ExampleDate));
            add(new User(2, "Rose", ExampleDate));
            add(new User(3, "Montrose", ExampleDate));
        }});

        //WHERE name NOT LIKE 'Mont%'
        Conditions conditionsA = new Conditions();
        conditionsA.like("name", "Mont%");
        List<String> resultsA = uRepo.findAttribute("name", conditionsA);
        assertEquals(2, resultsA.size());
        assertEquals("Mont", resultsA.get(0));
        assertEquals("Montrose", resultsA.get(1));

        // WHERE name LIKE 'Mont%'
        Conditions conditionsB = new Conditions();
        conditionsB.notLike("name", "Mont%");
        List<String> resultsB = uRepo.findAttribute("name", conditionsB);
        assertEquals(1, resultsB.size());
        assertEquals("Rose", resultsB.get(0));
    }

    @Test
    public void emptyConditionsReturnAllRecords() {
        List<User> users = new ArrayList<User>(){{
            add(new User(1, "Mont", ExampleDate));
            add(new User(2, "Rose", ExampleDate));
            add(new User(3, "Montrose", ExampleDate));
        }};
        saveUsersData(users);

        // WHERE 1 = 1
        Conditions c = new Conditions();
        List<User> results = uRepo.find(c);
        assertEquals(users.size(), results.size());
    }

    @Test
    public void conditionsInAndNotIn(){
        saveUsersData(new ArrayList<User>(){{
            add(new User(1, "A", ExampleDate));
            add(new User(2, "B", ExampleDate));
            add(new User(3, "C", ExampleDate));
        }});

        List<String> names = new ArrayList<>(Arrays.asList("B", "C"));
        Conditions cA = new Conditions();
        cA.in("name", names);

        List<String> namesReadA = uRepo.findAttribute("name", cA);
        assertEquals(namesReadA.size(), 2);
        assertTrue(collectionsAreEqual(names, namesReadA));

        Conditions cB = new Conditions();
        cB.notIn("name", names);

        List<String> namesReadB = uRepo.findAttribute("name", cB);
        assertEquals(namesReadB.size(), 1);
        assertFalse(names.contains(namesReadB.get(0)));
    }
}
