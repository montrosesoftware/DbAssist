package com.montrosesoftware.repositories;

import com.montrosesoftware.DateUtils;
import com.montrosesoftware.config.BaseTest;
import com.montrosesoftware.entities.User;
import org.junit.Test;
import org.junit.internal.runners.statements.FailOnTimeout;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.Tuple;
import java.util.*;

import static com.montrosesoftware.helpers.TestUtils.addMinutes;
import static com.montrosesoftware.helpers.TestUtils.collectionsAreEqual;
import static com.montrosesoftware.helpers.TestUtils.saveUsersData;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DbAssistOrderByGroupByTest extends BaseTest {

    private static final Date ExampleDate = DateUtils.getUtc("2012-06-12 08:10:15");
    private static final double Delta = 1e-15;
    @Autowired
    private UserRepo uRepo;

    @Test
    public void orderByDateUse() {
        Date date1 = ExampleDate;
        Date date2 = addMinutes(ExampleDate, 10);
        saveUsersData(uRepo, new ArrayList<User>() {{
            add(new User(1, "B", date2));
            add(new User(2, "A", date2));
            add(new User(3, "A", date1));
        }});

        ConditionsBuilder cb = new ConditionsBuilder();
        OrderBy orderBy = new OrderBy();
        orderBy
                .asc(cb, "name")
                .desc(cb, "createdAt");

        List<User> results = uRepo.find(cb, orderBy);

        assertEquals(results.size(), 3);
        assertTrue(results.get(0).getId() == 2);
        assertTrue(results.get(1).getId() == 3);
        assertTrue(results.get(2).getId() == 1);
    }

    @Test
    public void groupByWithAggregates() {
        saveUsersData(uRepo, new ArrayList<User>() {{
            add(new User(1, "Mont", ExampleDate, 14.5, "worker"));
            add(new User(2, "Mont", ExampleDate, 10.1, "worker"));
            add(new User(3, "Rose", ExampleDate, 1.5, "worker"));
            add(new User(4, "Rose", ExampleDate, 111.5, "boss"));
        }});

        ConditionsBuilder cb = new ConditionsBuilder();
        HierarchyCondition hc = cb.equal("category", "worker");
        cb.apply(hc);

        //select
        SelectionList selectionList = new SelectionList();
        selectionList
                .sum(cb, "salary")
                .avg(cb, "salary")
                .count(cb, "id");

        //TODO add more aggregates

        AbstractRepository.GroupBy<User> groupBy = (root) -> Arrays.asList(
                root.get("category")
        );

        List<Tuple> results = uRepo.findAttributes(selectionList, cb, null, null, groupBy);
        Tuple groupWorkersResults = results.get(0);

        Double sumSalaryWorkers = (Double) groupWorkersResults.get(0);
        Double avgSalaryWorkers = (Double) groupWorkersResults.get(1);
        Long numWorkers = (Long) groupWorkersResults.get(2);

        assertEquals(numWorkers.longValue(), 3);
        assertEquals(sumSalaryWorkers, 14.5 + 10.1 + 1.5, Delta);
        assertEquals(avgSalaryWorkers, sumSalaryWorkers / numWorkers, Delta);
    }

    @Test
    public void findAttributeWithOrderByUse() {
        Date date = DateUtils.getUtc("2012-06-12 08:10:15");
        Date dateAnother = DateUtils.getUtc("2000-03-03 11:10:15");
        saveUsersData(uRepo, new ArrayList<User>() {{
            add(new User(1, "A", date));
            add(new User(2, "B", dateAnother));
            add(new User(3, "C", date));
        }});

        ConditionsBuilder cb = new ConditionsBuilder();
        HierarchyCondition hc = cb.equal("createdAt", date);
        cb.apply(hc);

        //sort
        OrderBy orderBy = new OrderBy();
        orderBy.desc(cb, "name");

        List<String> namesRead = uRepo.findAttribute("name", cb, null, orderBy, null);

        assertEquals(namesRead.size(), 2);
        assertEquals(namesRead.get(0), "C");
        assertEquals(namesRead.get(1), "A");
    }

    @Test
    public void findAttributesWithOrderByUse() {
        saveUsersData(uRepo, new ArrayList<User>() {{
            add(new User(1, "Rose", ExampleDate));
            add(new User(2, "Mont", ExampleDate));
            add(new User(3, "Montrose", ExampleDate));
        }});

        ConditionsBuilder cb = new ConditionsBuilder();
        HierarchyCondition hc = cb.inRangeCondition("id", 1, 2);
        cb.apply(hc);

        //selects
        SelectionList selectionList = new SelectionList();
        selectionList
                .select(cb, "id")
                .select(cb, "name");

        //sort
        OrderBy orderBy = new OrderBy();
        orderBy.asc(cb, "name");

        List<Tuple> tuples = uRepo.findAttributes(selectionList, cb, orderBy);
        List<Integer> idsRead = new ArrayList<>();
        List<String> namesRead = new ArrayList<>();
        tuples.forEach((tuple -> {
            idsRead.add((Integer) tuple.get(0));
            namesRead.add((String) tuple.get(1));
        }));

        assertEquals(idsRead.get(0).intValue(), 2);
        assertEquals(idsRead.get(1).intValue(), 1);
        assertEquals(namesRead.get(0), "Mont");
        assertEquals(namesRead.get(1), "Rose");
    }
}
