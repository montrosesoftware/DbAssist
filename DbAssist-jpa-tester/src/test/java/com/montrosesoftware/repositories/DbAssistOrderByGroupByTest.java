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

import static com.montrosesoftware.repositories.TestUtils.addMinutes;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DbAssistOrderByGroupByTest extends BaseTest {

    @Autowired
    private UserRepo uRepo;

    private void saveUsersData(List<User> usersToSave){
        usersToSave.forEach(uRepo::save);
        uRepo.clearPersistenceContext();
    }

    private static final Date ExampleDate = DateUtils.getUtc("2012-06-12 08:10:15");

    @Test
    public void orderByDateUse(){
        Date date1 = ExampleDate;
        Date date2 = addMinutes(ExampleDate, 10);
        saveUsersData(new ArrayList<User>(){{
            add(new User(1, "B", date2));
            add(new User(2, "A", date2));
            add(new User(3, "A", date1));
        }});

        Conditions c = new Conditions();
        AbstractRepository.OrderBy<User> userOrderBy = (cb, root) -> Arrays.asList(
                cb.asc(root.get("name")),
                cb.desc(root.get("createdAt"))
                );
        List<User> results = uRepo.find(c, null, userOrderBy);

        assertEquals(results.size(), 3);
        assertTrue(results.get(0).getId() == 2);
        assertTrue(results.get(1).getId() == 3);
        assertTrue(results.get(2).getId() == 1);
    }
}
