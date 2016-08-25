package com.montrosesoftware.repositories;

import com.montrosesoftware.DateUtils;
import com.montrosesoftware.config.BaseTest;
import com.montrosesoftware.entities.Certificate;
import com.montrosesoftware.entities.User;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Timestamp;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DbAssistDateShiftTest extends BaseTest{

    @Autowired
    private UserRepo uRepo;

    @Autowired
    private CertificateRepo cRepo;

    @Test public void repositoriesProperlyAutowiredTest(){
        assertNotNull(uRepo);
        assertNotNull(cRepo);
    }

    private User getExampleUserData(){
        //prepare example user data
        Date expectedDatetime = DateUtils.getUtc("2016-06-12 14:54:15");
        Date expectedDate = DateUtils.getUtc("2016-03-02 00:00:00");
        Timestamp expectedTimestamp = new Timestamp(expectedDatetime.getTime());
        User user = new User(1, "Adam Spring", expectedDatetime, expectedTimestamp, expectedDate);
        return user;
    }

    private void assertTimeInDatesNotShifted(User userExpected, User userActual){
        assertNotNull(userActual);
        assertEquals("Names are not the same", userExpected.getName(), userActual.getName());
        assertEquals("Datetimes are not the same", userExpected.getCreatedAt(), userActual.getCreatedAt());
        assertEquals("Timestamps are not the same", userExpected.getUpdatedAt(), userActual.getUpdatedAt());
        assertEquals("Dates are not the same", userExpected.getLastLoggedAt(), userActual.getLastLoggedAt());
    }

    @Test
    public void dataInsertedByPlainSQLAndReadByJPAIsEqual(){
        User userExpected = getExampleUserData();
        uRepo.saveAsPlainSQL(userExpected);
        User userActual = uRepo.get(1);
        assertTimeInDatesNotShifted(userExpected, userActual);
    }

    @Test
    public void dataInsertedByPlainSQLAndReadUsingCriteriaIsEqual(){
        User userExpected = getExampleUserData();
        uRepo.saveAsPlainSQL(userExpected);
        User userActual = uRepo.getUsingCriteria();
        assertTimeInDatesNotShifted(userExpected, userActual);
    }

    @Test
    public void dataInsertedByPlainSQLAndReadUsingSpecificationIsEqual(){
        User userExpected = getExampleUserData();
        uRepo.saveAsPlainSQL(userExpected);
        User userActual = uRepo.getUsingSpecification(userExpected.getCreatedAt());
        assertTimeInDatesNotShifted(userExpected, userActual);
    }

    @Test
    public void dataInsertedByPlainSQLAndReadUsingDbAssistConditions(){
        User userExpected = getExampleUserData();
        uRepo.saveAsPlainSQL(userExpected);
        User userActual = uRepo.getUsingConditionsBuilder(userExpected.getCreatedAt());
        assertTimeInDatesNotShifted(userExpected, userActual);
    }

    @Test
    public void dataInsertedByJPAAndReadByPlainSQLIsNotEqual(){
        User userToInsert = getExampleUserData();

        //Plain SQL
        uRepo.saveAsPlainSQL(userToInsert);

        //JPA
        userToInsert.setId(userToInsert.getId()+1);
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

    @Test
    public void certificateRead(){
        Date expectedDate = DateUtils.getUtc("2016-06-12 14:54:15");
        Certificate certToInsert = new Certificate(1, "BHP", expectedDate);
        cRepo.saveAsPlainSQL(certToInsert);
        Certificate certRead = cRepo.get(1);
        assertNotNull(certRead);
    }

    @Test
    public void writeAndReadByHibernateWithJoinedEntities(){
        User userExpected = getExampleUserData();
        Certificate certActualA = new Certificate(1, "BHP", DateUtils.getUtc("2016-06-12 14:54:15"));
        Certificate certActualB = new Certificate(2, "Java Cert", DateUtils.getUtc("2014-03-12 11:11:15"));
        userExpected.addCertificate(certActualA);
        userExpected.addCertificate(certActualB);

        uRepo.save(userExpected);
        uRepo.clearPersistenceContext();

        User userActual = uRepo.get(1);
        assertNotNull(userActual);

        List<Certificate> certsRead = new ArrayList<>(userActual.getCertificates());
        assertEquals(2, certsRead.size());

        Certificate certExpectedA = certsRead.stream().filter(c -> c.getId() == 1).findFirst().get();
        Certificate certExpectedB = certsRead.stream().filter(c -> c.getId() == 2).findFirst().get();

        assertTimeInDatesNotShifted(userExpected, userActual);
        assertEquals("Certificate A dates are not the same", certActualA.getExpirationDate(), certExpectedA.getExpirationDate());
        assertEquals("Certificate A dates are not the same", certActualB.getExpirationDate(), certExpectedB.getExpirationDate());
    }
}
