package com.montrosesoftware.repositories;

import com.montrosesoftware.DateUtils;
import com.montrosesoftware.config.BaseTest;
import com.montrosesoftware.entities.User;
import com.montrosesoftware.repositories.UserRepo;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class DbTest extends BaseTest{

    @Autowired
    private UserRepo uRepo;

    @Test
    public void dataInsertedByPlainAndReadBySpringIsNotEqual(){
        Logger.getRootLogger().setLevel(Level.ERROR);

        String expDateString = "2016-06-12 14:54:15";
        Date expectedDate = DateUtils.getUtc(expDateString);
        int id = 1;
        User userToInsert = new User(id, "Adam Annot", expectedDate);

        Assert.assertNotNull(uRepo);
        uRepo.saveAsPlainSQL(userToInsert);

        User userRead = uRepo.get(1);
        Assert.assertNotNull(userRead);

        assertEquals("Names are not the same", userToInsert.getName(), userRead.getName());
        assertEquals("Dates are not the same", userToInsert.getCreatedAt(), userRead.getCreatedAt());
    }

}
