package com.montrosesoftware.repositories;

import com.montrosesoftware.DateUtils;
import com.montrosesoftware.config.BaseTest;
import com.montrosesoftware.entities.Certificate;
import com.montrosesoftware.entities.User;
import com.montrosesoftware.repositories.CertificateRepo;
import com.montrosesoftware.repositories.Conditions;
import com.montrosesoftware.repositories.UserRepo;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DbAssistDateShiftTest extends BaseTest{

    @Autowired
    private UserRepo uRepo;

    @Autowired
    private CertificateRepo cRepo;

    @Test public void reposProperlyAutowiredTest(){
        assertNotNull(uRepo);
        assertNotNull(cRepo);
    }

    @Test
    public void dataInsertedByPlainSQLAndReadBySpringIsNotEqual(){
        Logger.getRootLogger().setLevel(Level.ERROR);

        //prepare user
        Date expectedDate = DateUtils.getUtc("2016-06-12 14:54:15");
        int id = 1;
        User userToInsert = new User(id, "Adam Spring", expectedDate);

        uRepo.saveAsPlainSQL(userToInsert);

        User userReadSpring = uRepo.get(id);

        assertNotNull(userReadSpring);
        assertEquals("Names are not the same", userToInsert.getName(), userReadSpring.getName());
        assertEquals("Dates are not the same", userToInsert.getCreatedAt(), userReadSpring.getCreatedAt());
    }

    @Test
    public void dataInsertedBySQLAndReadUsingCriteriaIsNotEqual(){
        Logger.getRootLogger().setLevel(Level.ERROR);

        //prepare user
        Date expectedDate = DateUtils.getUtc("2016-06-12 12:10:15");
        int id = 1;
        User userToInsert = new User(id, "Adam Spring", expectedDate);

        //insert
        uRepo.saveAsPlainSQL(userToInsert);

        //read
        List users = uRepo.getUsingCriteria();
        assertEquals(1, users.size());
        User userReadSpring = (User) users.get(0);

        assertNotNull(userReadSpring);
        assertEquals("Names are not the same", userToInsert.getName(), userReadSpring.getName());
        assertEquals("Dates are not the same", userToInsert.getCreatedAt(), userReadSpring.getCreatedAt());
    }

    @Test
    public void dataInsertedBySQLAndReadUsingSpecificationIsNotEqual(){
        Logger.getRootLogger().setLevel(Level.ERROR);

        //prepare user
        Date expectedDate = DateUtils.getUtc("2016-06-12 12:10:15");
        int id = 1;
        User userToInsert = new User(id, "Adam Spring", expectedDate);

        //insert
        uRepo.saveAsPlainSQL(userToInsert);

        //read
        User userReadUsingSpecs = uRepo.getUsingSpecification(expectedDate);

        assertNotNull(userReadUsingSpecs);
        assertEquals("Names are not the same", userToInsert.getName(), userReadUsingSpecs.getName());
        assertEquals("Dates are not the same", userToInsert.getCreatedAt(), userReadUsingSpecs.getCreatedAt());
    }

    @Test
    public void dataInsertedBySQLAndReadUsingDbAssistConditions(){
        Logger.getRootLogger().setLevel(Level.ERROR);

        //prepare user
        Date expectedDate = DateUtils.getUtc("2016-06-12 08:10:15");
        int id = 1;
        User userToInsert = new User(id, "Joanna Spring", expectedDate);

        //insert
        uRepo.saveAsPlainSQL(userToInsert);

        //read
        User userReadUsingSpecs = uRepo.getUsingConditions(expectedDate);

        assertNotNull(userReadUsingSpecs);
        assertEquals("Names are not the same", userToInsert.getName(), userReadUsingSpecs.getName());
        assertEquals("Dates are not the same", userToInsert.getCreatedAt(), userReadUsingSpecs.getCreatedAt());
    }

    @Test
    public void dataInsertedBySpringAndReadByPlainSQLIsNotEqual(){
        Logger.getRootLogger().setLevel(Level.ERROR);

        Date expectedDate = DateUtils.getUtc("2016-06-12 15:15:15");
        int id = 1;
        User userToInsert = new User(id, "Adam Spring", expectedDate);

        //Plain SQL
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

    @Test
    public void certificateRead(){
        Logger.getRootLogger().setLevel(Level.ERROR);

        //prepare example certificate
        Date expectedDate = DateUtils.getUtc("2016-06-12 14:54:15");
        int id = 1;
        Certificate certToInsert = new Certificate(1, "BHP", expectedDate);

        cRepo.saveAsPlainSQL(certToInsert);

        Certificate certRead = cRepo.get(id);
        assertNotNull(certRead);
    }

    @Test
    public void writeAndReadByHibernateJoin(){
        Logger.getRootLogger().setLevel(Level.ERROR);

        //prepare user
        Date expectedDate = DateUtils.getUtc("2016-06-12 08:10:15");
        int id = 1;
        User userToInsert = new User(id, "Joanna Spring", expectedDate);

        //prepare certificates
        Date certDateA = DateUtils.getUtc("2016-06-12 14:54:15");
        Certificate certA = new Certificate(1, "BHP", certDateA);

        Date certDateB = DateUtils.getUtc("2014-03-12 11:11:15");
        Certificate certB = new Certificate(2, "Java Cert", certDateB);

        userToInsert.addCertificate(certA);
        userToInsert.addCertificate(certB);

        uRepo.save(userToInsert);
        uRepo.clearPersistenceContext();

        User userRead = uRepo.get(id);
        assertNotNull(userRead);

        List<Certificate> certsRead = userRead.getCertificates();
        assertEquals(2, certsRead.size());

        assertEquals("User dates are not the same", userToInsert.getCreatedAt(), userRead.getCreatedAt());
        assertEquals("Certificate A dates are not the same", certA.getExpirationDate(), certsRead.get(0).getExpirationDate());
        assertEquals("Certificate B dates are not the same", certB.getExpirationDate(), certsRead.get(1).getExpirationDate());
    }
}
