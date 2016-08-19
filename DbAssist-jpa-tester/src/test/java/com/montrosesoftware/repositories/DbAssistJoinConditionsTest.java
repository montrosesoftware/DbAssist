package com.montrosesoftware.repositories;

import com.montrosesoftware.DateUtils;
import com.montrosesoftware.config.BaseTest;
import com.montrosesoftware.entities.Certificate;
import com.montrosesoftware.entities.Provider;
import com.montrosesoftware.entities.User;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.criteria.JoinType;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static com.montrosesoftware.repositories.TestUtils.saveUsersData;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DbAssistJoinConditionsTest extends BaseTest {

    @Autowired
    private UserRepo uRepo;

    @Autowired
    private CertificateRepo cRepo;

    @Autowired
    private ProviderRepo pRepo;

    @Test
    public void findWithJoinConditionsUse(){
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
        saveUsersData(uRepo, Arrays.asList(userA, userB, userC));

        ConditionsBuilder conditionsBuilder = new ConditionsBuilder();
        conditionsBuilder.equal("createdAt", expectedDate);

        //add join conditions
        ConditionsBuilder certificatesConditionsBuilder = conditionsBuilder.getJoinConditionsBuilder("certificates", JoinType.LEFT);
        certificatesConditionsBuilder.or(
                certificatesConditionsBuilder.equal("name", "BHP"),
                certificatesConditionsBuilder.equal("name", "Java Cert")
        );

        // SELECT ... LEFT OUTER JOIN ... ON ... WHERE users.created_at = ? AND (certificates.name = ? OR certificates.name = ?)
        List<User> results = uRepo.find(conditionsBuilder);
        assertEquals(2,results.size());
        List<Certificate> certsOfUserA = results.get(0).getCertificates();
        List<Certificate> certsOfUserB = results.get(1).getCertificates();
        assertTrue(certsOfUserA.contains(certA) || certsOfUserA.contains(certB));
        assertTrue(certsOfUserB.contains(certA) || certsOfUserB.contains(certB));
    }

    @Test
    public void findWithJoinConditionsAndLogicalOrBetweenTables(){
        //TODO ms: remove duplication
        Date certDateA = DateUtils.getUtc("2016-06-12 14:54:15");
        Certificate certA = new Certificate(1, "BHP", certDateA);
        Date certDateB = DateUtils.getUtc("2014-03-12 11:11:15");
        Certificate certB = new Certificate(2, "Java Cert", certDateB);

        //prepare users
        Date dateA = DateUtils.getUtc("2016-06-12 08:10:15");
        Date dateB = DateUtils.getUtc("2010-06-12 08:10:15");
        User userA = new User(1, "Rose", dateA);
        User userB = new User(2, "Mont", dateB);
        User userC = new User(3, "Tom", dateB);
        userA.addCertificate(certA);
        userA.addCertificate(certB);
        userB.addCertificate(certB);
        saveUsersData(uRepo, Arrays.asList(userA, userB, userC));

        ConditionsBuilder conditionsBuilder = new ConditionsBuilder();
        ConditionsBuilder.Condition createdAt = conditionsBuilder.equal("createdAt", dateA);

        //add join conditions
        ConditionsBuilder certificatesConditionsBuilder = conditionsBuilder.getJoinConditionsBuilder("certificates", JoinType.LEFT);
        ConditionsBuilder.Condition name = certificatesConditionsBuilder.equal("name", "BHP");
        conditionsBuilder.or(createdAt, certificatesConditionsBuilder, name);
        certificatesConditionsBuilder.equal("name", "Java Cert");

        // SELECT ... LEFT OUTER JOIN ... ON ... WHERE (users.created_at = ? OR certificates.name = ?) AND certificates.name = ?
        List<User> results = uRepo.find(conditionsBuilder);
        assertEquals(1,results.size());
        assertEquals(1, results.get(0).getId());
    }

    @Test
    public void findWithJoinConditionsAndLogicalAndBetweenTables(){
        Date certDateA = DateUtils.getUtc("2016-06-12 14:54:15");
        Certificate certA = new Certificate(1, "BHP", certDateA);
        Date certDateB = DateUtils.getUtc("2014-03-12 11:11:15");
        Certificate certB = new Certificate(2, "Java Cert", certDateB);

        //prepare users
        Date dateA = DateUtils.getUtc("2016-06-12 06:10:15");
        Date dateB = DateUtils.getUtc("2010-06-12 08:10:15");
        User userA = new User(1, "Rose", dateA);
        User userB = new User(2, "Mont", dateB);
        User userC = new User(3, "Tom", dateB);
        userA.addCertificate(certA);
        userA.addCertificate(certB);
        userB.addCertificate(certB);
        saveUsersData(uRepo, Arrays.asList(userA, userB, userC));

        ConditionsBuilder conditionsBuilder = new ConditionsBuilder();
        ConditionsBuilder.Condition createdAt = conditionsBuilder.equal("createdAt", dateA);

        //add join conditions
        ConditionsBuilder certificatesConditionsBuilder = conditionsBuilder.getJoinConditionsBuilder("certificates", JoinType.LEFT);
        ConditionsBuilder.Condition name = certificatesConditionsBuilder.equal("name", "BHP");

        ConditionsBuilder.Condition id = conditionsBuilder.equal("id", 2);

        conditionsBuilder.or(
                conditionsBuilder.and(createdAt, certificatesConditionsBuilder, name),
                id
        );

        // SELECT ... LEFT OUTER JOIN ... ON ... WHERE (users.created_at = ? AND certificates.name = ?) OR users.id = ?
        List<User> results = uRepo.find(conditionsBuilder);
        assertEquals(2, results.size());
    }

    @Test
    public void findWithJoinConditionsAndLogicalAndOrCombination(){
        Date certDateA = DateUtils.getUtc("2016-06-12 14:54:15");
        Certificate certA = new Certificate(1, "BHP", certDateA);
        Date certDateB = DateUtils.getUtc("2014-03-12 11:11:15");
        Certificate certB = new Certificate(2, "Java Cert", certDateB);

        //prepare users
        Date dateA = DateUtils.getUtc("2016-06-12 07:10:15");
        Date dateB = DateUtils.getUtc("2010-06-12 08:10:15");
        User userA = new User(1, "Rose", dateA);
        User userB = new User(2, "Mont", dateB);
        User userC = new User(3, "Tom", dateB);
        userA.addCertificate(certA);
        userA.addCertificate(certB);
        userB.addCertificate(certB);
        saveUsersData(uRepo, Arrays.asList(userA, userB, userC));

        ConditionsBuilder conditionsBuilder = new ConditionsBuilder();
        ConditionsBuilder.Condition createdAt = conditionsBuilder.equal("createdAt", dateA);

        //add join conditions
        ConditionsBuilder certificatesConditionsBuilder = conditionsBuilder.getJoinConditionsBuilder("certificates", JoinType.LEFT);
        ConditionsBuilder.Condition name = certificatesConditionsBuilder.equal("name", "BHP");

        ConditionsBuilder.Condition id = conditionsBuilder.equal("id", 1);

        conditionsBuilder.or(
                conditionsBuilder.and(createdAt, certificatesConditionsBuilder, name),
                certificatesConditionsBuilder,
                id
        );

        // SELECT ... LEFT OUTER JOIN ... ON ... WHERE (users.created_at = ? AND certificates.name = ?) OR certificates.id = ?
        List<User> results = uRepo.find(conditionsBuilder);
        assertEquals(1, results.size());
    }

    @Test
    public void multipleJoinsConditionBuildersUse(){
        //TODO test is not finished
        //Certificate providers and certificates
        Provider providerA = new Provider(1, "Provider 1", true);
        Provider providerB = new Provider(2, "Provider 2", false);

        Date certDateA = DateUtils.getUtc("2016-06-12 14:54:15");
        Certificate certA = new Certificate(1, "BHP", certDateA);
        certA.setProvider(providerA);

        Date certDateB = DateUtils.getUtc("2014-03-12 11:11:15");
        Certificate certB = new Certificate(2, "Java Cert", certDateB);
        certB.setProvider(providerB);

        pRepo.save(providerA);
        pRepo.save(providerB);

        //prepare users
        Date dateA = DateUtils.getUtc("2016-06-12 07:10:15");
        Date dateB = DateUtils.getUtc("2010-06-12 08:10:15");
        User userA = new User(1, "Rose", dateA);
        User userB = new User(2, "Mont", dateB);
        User userC = new User(3, "Tom", dateB);
        userA.addCertificate(certA);
        userA.addCertificate(certB);
        userB.addCertificate(certB);
        saveUsersData(uRepo, Arrays.asList(userA, userB, userC));

        //read with no conditions
        List<Provider> providersRead = pRepo.find(new ConditionsBuilder());
        List<User> usersRead = uRepo.find(new ConditionsBuilder());
        assertEquals(providersRead.size(), 2);
        assertEquals(usersRead.size(), 3);

        //complex example
        ConditionsBuilder builderUsers = new ConditionsBuilder();
        ConditionsBuilder.Condition uIdLessThan = builderUsers.lessThan("id", 15);
        ConditionsBuilder.Condition uIdGreaterThanOrEqual = builderUsers.greaterThanOrEqualTo("id", 0);

        ConditionsBuilder builderCertificates = builderUsers.getJoinConditionsBuilder("certificates", JoinType.LEFT);
        ConditionsBuilder.Condition certIdLessThan = builderCertificates.lessThan("id", 5);
        ConditionsBuilder.Condition certIdGreaterThanOrEqual = builderCertificates.greaterThanOrEqualTo("id", 0);

        ConditionsBuilder builderProviders = builderCertificates.getJoinConditionsBuilder("provider", JoinType.LEFT);
        ConditionsBuilder.Condition provNameA = builderProviders.equal("name", "Provider 1");
        ConditionsBuilder.Condition provNameB = builderProviders.equal("name", "Provider 2");

        // remake it
        /*builderUsers.or(
                builderUsers.and(uIdLessThan, null, uIdGreaterThanOrEqual),
                null,
                builderProviders.or(
                        builderCertificates,
                        builderCertificates.and(certIdGreaterThanOrEqual, certIdLessThan),
                        builderProviders,
                        builderProviders.or(provNameA, provNameB)
                )
        );
        */
        //TODO change the condition to boolean active!

        List<User> usersReadMultipleJoin = uRepo.find(builderUsers);
        usersReadMultipleJoin.size();

    }
}
