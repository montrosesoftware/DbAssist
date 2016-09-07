package com.montrosesoftware.dbassist.repositories;

import com.montrosesoftware.dbassist.DateUtils;
import com.montrosesoftware.dbassist.config.BaseTest;
import com.montrosesoftware.dbassist.entities.User;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.Tuple;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.montrosesoftware.dbassist.helpers.TestUtils.addMinutes;
import static com.montrosesoftware.dbassist.helpers.TestUtils.saveUsersData;
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
                .desc(cb, "createdAtUtc");

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

        //select TODO add more aggregates (least and greatest)
        SelectionList selectionList = new SelectionList();
        selectionList
                .sum(cb, "salary")
                .avg(cb, "salary")
                .count(cb, "id")
                .sumAsLong(cb, "id")
                .sumAsDouble(cb, "salary")
                .min(cb, "salary")
                .max(cb, "salary");

        GroupBy groupBy = new GroupBy();
        groupBy.groupBy(cb, "category");

        List<Tuple> results = uRepo.findAttributes(selectionList, cb, groupBy);
        Tuple groupWorkersResults = results.get(0);

        Double sumSalaryWorkers = (Double) groupWorkersResults.get(0);
        Double avgSalaryWorkers = (Double) groupWorkersResults.get(1);
        Long numWorkers = (Long) groupWorkersResults.get(2);
        Long sumAsLongIdsWorkers = (Long) groupWorkersResults.get(3);
        Double sumAsDoubleSalariesWorkers = (Double) groupWorkersResults.get(4);
        Double minSalaryWorkers = (Double) groupWorkersResults.get(5);
        Double maxSalaryWorkers = (Double) groupWorkersResults.get(6);

        assertEquals(numWorkers.longValue(), 3);
        assertEquals(sumSalaryWorkers, 14.5 + 10.1 + 1.5, Delta);
        assertEquals(avgSalaryWorkers, sumSalaryWorkers / numWorkers, Delta);
        assertEquals(sumAsLongIdsWorkers.longValue(), 6);
        assertEquals(sumAsDoubleSalariesWorkers, 14.5 + 10.1 + 1.5, Delta);
        assertEquals(minSalaryWorkers, 1.5, Delta);
        assertEquals(maxSalaryWorkers, 14.5, Delta);
    }

    @Test
    public void groupByMultipleColumns() {
        saveUsersData(uRepo, new ArrayList<User>() {{
            add(new User(1, "Mont", ExampleDate, 14.5, "worker"));
            add(new User(2, "Mont", ExampleDate, 10.1, "worker"));
            add(new User(3, "Rose", ExampleDate, 1.5, "worker"));
            add(new User(4, "Rose", ExampleDate, 111.5, "boss"));
        }});

        //conditions
        ConditionsBuilder cb = new ConditionsBuilder();
        HierarchyCondition hc = cb.equal("category", "worker");
        cb.apply(hc);

        //select
        SelectionList selectionList = new SelectionList();
        selectionList.avg(cb, "salary");

        //group by
        GroupBy groupBy = new GroupBy();
        groupBy
                .groupBy(cb, "category")
                .groupBy(cb, "name");

        //order by
        OrderBy orderBy = new OrderBy();
        orderBy.desc(cb, "name");

        List<Tuple> results = uRepo.findAttributes(selectionList, cb, orderBy, groupBy);

        assertEquals(results.size(), 2);
        Double avgGroupA = (Double) results.get(0).get(0);
        Double avgGroupB = (Double) results.get(1).get(0);

        assertEquals(avgGroupA, 1.5, Delta);
        assertEquals(avgGroupB, (14.5 + 10.1) / 2, Delta);
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
        HierarchyCondition hc = cb.equal("createdAtUtc", date);
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
