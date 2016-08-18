package com.montrosesoftware.repositories;

import com.montrosesoftware.DateUtils;
import com.montrosesoftware.config.BaseTest;
import com.montrosesoftware.entities.Certificate;
import com.montrosesoftware.entities.User;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.criteria.JoinType;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class DbAssistJoinConditionsTest extends BaseTest {

    @Autowired
    private UserRepo uRepo;

    @Autowired
    private CertificateRepo cRepo;

    private void saveUsersData(List<User> usersToSave){
        usersToSave.forEach(uRepo::save);
        uRepo.clearPersistenceContext();
    }

    @Test
    public void joinConditionsUse(){
        //prepare certificates
        Date certDateA = DateUtils.getUtc("2016-06-12 14:54:15");
        Certificate certA = new Certificate(1, "BHP", certDateA);
        Date certDateB = DateUtils.getUtc("2014-03-12 11:11:15");
        Certificate certB = new Certificate(2, "Java Cert", certDateB);

        //prepare users
        Date expectedDate = DateUtils.getUtc("2016-06-12 08:10:15");
        User userA = new User(1, "Rose", expectedDate);
        User userB = new User(2, "Mont", expectedDate);
        User userC = new User(3, "Tom", expectedDate);
        userA.addCertificate(certA);
        userA.addCertificate(certB);
        userB.addCertificate(certB);
        saveUsersData(Arrays.asList(userA, userB, userC));

        Conditions conditions = new Conditions();
        conditions.equal("createdAt", expectedDate);

        //add join conditions
        Conditions joinConditions = conditions.getJoinConditions("certificates", JoinType.LEFT);
        joinConditions.or(
                joinConditions.equal("name", "BHP"),
                joinConditions.equal("name", "Java Cert")
        );

        List<User> results = uRepo.find(conditions);
        assertEquals(2,results.size());

        //TODO
//        User userRead = results.get(0);
//        List<Certificate> certsRead = userRead.getCertificates();
//        assertEquals(2, certsRead.size());
//
//        assertEquals("User dates are not the same", userA.getCreatedAt(), userRead.getCreatedAt());
//        assertEquals("Certificate A dates are not the same", certA.getExpirationDate(), certsRead.get(0).getExpirationDate());
//        assertEquals("Certificate B dates are not the same", certB.getExpirationDate(), certsRead.get(1).getExpirationDate());
    }
}
