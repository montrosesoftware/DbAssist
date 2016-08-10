package com.montrosesoftware.repositories;

import com.montrosesoftware.DateUtils;
import com.montrosesoftware.config.BaseTest;
import com.montrosesoftware.entities.User;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class DbTest extends BaseTest{

    @Autowired
    private UserRepo uRepo;

    @Test
    public void dataInsertedByPlainSQLAndReadBySpringIsNotEqual(){
        Logger.getRootLogger().setLevel(Level.ERROR);

        //prepare user
        String expDateString = "2016-06-12 14:54:15";
        Date expectedDate = DateUtils.getUtc(expDateString);
        int id = 1;
        User userToInsert = new User(id, "Adam Spring", expectedDate);

        Assert.assertNotNull(uRepo);
        uRepo.saveAsPlainSQL(userToInsert);

        User userReadSpring = uRepo.get(id);

        Assert.assertNotNull(userReadSpring);
        assertEquals("Names are not the same", userToInsert.getName(), userReadSpring.getName());
        assertEquals("Dates are not the same", userToInsert.getCreatedAt(), userReadSpring.getCreatedAt());
    }

    @Test
    public void dataInsertedBySQLAndReadUsingCriteriaIsNotEqual(){
        Logger.getRootLogger().setLevel(Level.ERROR);

        //prepare user
        String expDateString = "2016-06-12 12:10:15";
        Date expectedDate = DateUtils.getUtc(expDateString);
        int id = 1;
        User userToInsert = new User(id, "Adam Spring", expectedDate);

        //insert
        Assert.assertNotNull(uRepo);
        uRepo.saveAsPlainSQL(userToInsert);

        //read
        List users = uRepo.getUsingCriteria();
        assertEquals(1, users.size());
        User userReadSpring = (User) users.get(0);

        Assert.assertNotNull(userReadSpring);
        assertEquals("Names are not the same", userToInsert.getName(), userReadSpring.getName());
        assertEquals("Dates are not the same", userToInsert.getCreatedAt(), userReadSpring.getCreatedAt());
    }

    @Test
    public void dataInsertedBySQLAndReadUsingSpecificationIsNotEqual(){
        Logger.getRootLogger().setLevel(Level.ERROR);

        //prepare user
        String expDateString = "2016-06-12 12:10:15";
        Date expectedDate = DateUtils.getUtc(expDateString);
        int id = 1;
        User userToInsert = new User(id, "Adam Spring", expectedDate);

        //insert
        Assert.assertNotNull(uRepo);
        uRepo.saveAsPlainSQL(userToInsert);

        //read
        User userReadUsingSpecs = uRepo.getUsingSpecification(expectedDate);

        Assert.assertNotNull(userReadUsingSpecs);
        assertEquals("Names are not the same", userToInsert.getName(), userReadUsingSpecs.getName());
        assertEquals("Dates are not the same", userToInsert.getCreatedAt(), userReadUsingSpecs.getCreatedAt());
    }

    @Test
    public void dataInsertedBySQLAndReadUsingDbAssistConditions(){
        Logger.getRootLogger().setLevel(Level.ERROR);

        //prepare user
        String expDateString = "2016-06-12 08:10:15";
        Date expectedDate = DateUtils.getUtc(expDateString);
        int id = 1;
        User userToInsert = new User(id, "Joanna Spring", expectedDate);

        //insert
        Assert.assertNotNull(uRepo);
        uRepo.saveAsPlainSQL(userToInsert);

        //read
        User userReadUsingSpecs = uRepo.getUsingConditions(expectedDate);

        Assert.assertNotNull(userReadUsingSpecs);
        assertEquals("Names are not the same", userToInsert.getName(), userReadUsingSpecs.getName());
        assertEquals("Dates are not the same", userToInsert.getCreatedAt(), userReadUsingSpecs.getCreatedAt());
    }

    @Test
    public void dataInsertedBySpringAndReadByPlainSQLIsNotEqual(){
        Logger.getRootLogger().setLevel(Level.ERROR);

        String expDateString = "2016-06-12 15:15:15";
        Date expectedDate = DateUtils.getUtc(expDateString);
        int id = 1;
        User userToInsert = new User(id, "Adam Spring", expectedDate);

        //Plain SQL
        Assert.assertNotNull(uRepo);
        uRepo.saveAsPlainSQL(userToInsert);

        //Spring
        userToInsert.setId(id+1);
        uRepo.save(userToInsert);

        //read and compare
        List<Object[]> userObjects = uRepo.getDataByPlainSQL();
        assertEquals(2, userObjects.size());

        int columnsAmount = userObjects.get(0).length;
        for (int i = 0; i < columnsAmount; i++) {
            Object[] first = userObjects.get(0);
            Object[] second = userObjects.get(1);
            assertEquals(first[i], second[i]);
        }
    }

}
