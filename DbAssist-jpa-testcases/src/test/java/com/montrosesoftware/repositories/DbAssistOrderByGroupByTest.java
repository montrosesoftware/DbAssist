package com.montrosesoftware.repositories;

import com.montrosesoftware.DateUtils;
import com.montrosesoftware.config.BaseTest;
import com.montrosesoftware.entities.User;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.Tuple;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static com.montrosesoftware.helpers.TestUtils.addMinutes;
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

        AbstractRepository.OrderBy<User> userOrderBy = (builder, root) -> Arrays.asList(
                builder.asc(root.get("name")),
                builder.desc(root.get("createdAt"))
        );
        List<User> results = uRepo.find(userOrderBy);

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

        AbstractRepository.SelectionList<User> selectionList = (builder, root) -> Arrays.asList(
                builder.sum(root.get("salary")),
                builder.avg(root.get("salary")),
                builder.count(root.get("id"))
        );

        ConditionsBuilder conditions = new ConditionsBuilder();
        HierarchyCondition hc = conditions.equal("category", "worker");
        conditions.apply(hc);

        AbstractRepository.GroupBy<User> groupBy = (root) -> Arrays.asList(
                root.get("category")
        );

        List<Tuple> results = uRepo.findAttributes(selectionList, conditions, null, null, groupBy);
        Tuple groupWorkersResults = results.get(0);

        Double sumSalaryWorkers = (Double) groupWorkersResults.get(0);
        Double avgSalaryWorkers = (Double) groupWorkersResults.get(1);
        Long numWorkers = (Long) groupWorkersResults.get(2);

        assertEquals(numWorkers.longValue(), 3);
        assertEquals(sumSalaryWorkers, 14.5 + 10.1 + 1.5, Delta);
        assertEquals(avgSalaryWorkers, sumSalaryWorkers / numWorkers, Delta);
    }
}
