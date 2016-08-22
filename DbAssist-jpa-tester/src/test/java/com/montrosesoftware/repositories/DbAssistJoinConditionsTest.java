package com.montrosesoftware.repositories;

import com.montrosesoftware.DateUtils;
import com.montrosesoftware.config.BaseTest;
import com.montrosesoftware.entities.Certificate;
import com.montrosesoftware.entities.Provider;
import com.montrosesoftware.entities.User;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.ArrayList;
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
      //  ConditionsBuilder.Condition uIdGreaterThanOrEqual = builderUsers.greaterThanOrEqualTo("id", 0);

        ConditionsBuilder builderCertificates = builderUsers.getJoinConditionsBuilder("certificates", JoinType.LEFT);
//        ConditionsBuilder.Condition certIdLessThan = builderCertificates.lessThan("id", 5);
//        ConditionsBuilder.Condition certIdGreaterThanOrEqual = builderCertificates.greaterThanOrEqualTo("id", 0);

        ConditionsBuilder builderProviders = builderCertificates.getJoinConditionsBuilder("provider", JoinType.LEFT);
        ConditionsBuilder.Condition provNameA = builderProviders.equal("name", "Provider 1");
        //ConditionsBuilder.Condition provNameB = builderProviders.equal("name", "Provider 2");

        // TODO remake it
        builderUsers.or(
            uIdLessThan,
            provNameA
        );

        //TODO change the condition to boolean active!

        List<User> usersReadMultipleJoin = uRepo.find(builderUsers);
        usersReadMultipleJoin.size();
    }

    @Test
    public void WriteAndReadUsersOneToManyRelation(){
        //Certificate providers and certificates
        Provider providerA = new Provider(1, "Provider 1", true);
        Provider providerB = new Provider(2, "Provider 2", false);

        Date certDateA = DateUtils.getUtc("2016-06-12 14:54:15");
        Certificate certA = new Certificate(1, "BHP", certDateA);
        certA.setProvider(providerA);

        Date certDateB = DateUtils.getUtc("2014-03-12 11:11:15");
        Certificate certB = new Certificate(2, "Java Cert", certDateB);
        certB.setProvider(providerB);

        //prepare users
        Date date = DateUtils.getUtc("2010-06-12 08:10:15");
        User userA = new User(1, "Rose", date);
        User userB = new User(2, "Mont", date);
        User userC = new User(3, "Tom", date);
        userA.setMainCertificate(certA);
        userB.setMainCertificate(certB);

        saveUsersData(uRepo, Arrays.asList(userA, userB, userC));

        //read
        List<User> results = uRepo.find(new ConditionsBuilder());
        assertEquals(3, results.size());
    }

    @Test
    public void ComplexJoinWithOneToManyChainedRelation(){
        //Certificate providers and certificates
        Provider providerA = new Provider(1, "Provider 1", true);
        Provider providerB = new Provider(2, "Provider 2", false);

        Date certDateA = DateUtils.getUtc("2016-05-12 14:54:15");
        Certificate certA = new Certificate(1, "BHP", certDateA);
        certA.setProvider(providerA);

        Date certDateB = DateUtils.getUtc("2014-03-12 11:11:15");
        Certificate certB = new Certificate(2, "Java Cert", certDateB);
        certB.setProvider(providerB);

        //prepare users
        Date date = DateUtils.getUtc("2010-06-12 08:10:15");
        User userA = new User(1, "Rose", date);
        User userB = new User(2, "Mont", date);
        User userC = new User(3, "Tom", date);
        userA.setMainCertificate(certA);
        userB.setMainCertificate(certB);

        saveUsersData(uRepo, Arrays.asList(userA, userB, userC));

        //chain, one to many
        // user->certificate->provider
        ConditionsBuilder conditionsBuilder = new ConditionsBuilder();
        ConditionsBuilder builderCertificate = conditionsBuilder.getJoinConditionsBuilder("mainCertificate", JoinType.LEFT);
        ConditionsBuilder builderProvider = builderCertificate.getJoinConditionsBuilder("provider", JoinType.LEFT);

        ConditionsBuilder.Condition uIdGreaterThanOrEqual = conditionsBuilder.greaterThanOrEqualTo("id", 0);
        ConditionsBuilder.Condition uIdLessThan = conditionsBuilder.lessThan("id", 15);

        ConditionsBuilder.Condition certIdLessThan = builderCertificate.lessThan("id", 5);
        ConditionsBuilder.Condition certIdGreaterThanOrEqual = builderCertificate.greaterThanOrEqualTo("id", 0);

        ConditionsBuilder.Condition provNameA = builderProvider.equal("name", "Provider 1");
        ConditionsBuilder.Condition provNameB = builderProvider.equal("name", "Provider 2");

        conditionsBuilder.or(
                conditionsBuilder.and(uIdLessThan, uIdGreaterThanOrEqual),
                builderCertificate,
                builderCertificate.or(
                        builderCertificate,
                        builderCertificate.and(certIdGreaterThanOrEqual, certIdLessThan),
                        builderProvider,
                        builderProvider.or(provNameA, provNameB)
                    )
        );

        //ERROR

        List<User> results = uRepo.find(conditionsBuilder);
        //TODO finish
    }



    @Test
    public void ComplexJoinWithManyToManyNonChainRelation(){
        //Certificate providers and certificates
        Provider providerA = new Provider(1, "Provider 1", true);
        Provider providerB = new Provider(2, "Provider 2", false);

        Date certDateA = DateUtils.getUtc("2016-06-12 14:54:15");
        Certificate certA = new Certificate(1, "BHP", certDateA);
        certA.setProvider(providerA);

        Date certDateB = DateUtils.getUtc("2014-03-12 11:11:15");
        Certificate certB = new Certificate(2, "Java Cert", certDateB);
        certB.setProvider(providerB);

        //prepare users
        Date date = DateUtils.getUtc("2011-06-12 08:10:15");
        User userA = new User(1, "Rose", date);
        User userB = new User(2, "Mont", date);
        User userC = new User(3, "Tom", date);
        userA.addCertificate(certA);
        userB.addCertificate(certB);

        saveUsersData(uRepo, Arrays.asList(userA, userB, userC));

        //non-chain, one to many
        //certificate --> user
        //            --> provider

        ConditionsBuilder conditionsBuilder = new ConditionsBuilder();
        ConditionsBuilder builderUsers = conditionsBuilder.getJoinConditionsBuilder("users", JoinType.LEFT);
        ConditionsBuilder builderProviders = conditionsBuilder.getJoinConditionsBuilder("provider", JoinType.LEFT);


        ConditionsBuilder.Condition certIdGreaterThanOrEqual = conditionsBuilder.greaterThanOrEqualTo("id", 0);
        ConditionsBuilder.Condition certIdLessThan = conditionsBuilder.lessThan("id", 5);

        ConditionsBuilder.Condition uIdLessThan = builderUsers.lessThan("id", 15);
        ConditionsBuilder.Condition uIdGreaterThanOrEqual = builderUsers.greaterThanOrEqualTo("id", 0);

        ConditionsBuilder.Condition provNameA = builderProviders.equal("name", "Provider 1");
        ConditionsBuilder.Condition provNameB = builderProviders.equal("name", "Provider 2");

        conditionsBuilder.or(
                conditionsBuilder.and(uIdLessThan, uIdGreaterThanOrEqual),
                null,
                conditionsBuilder.or(
                        builderUsers,
                        builderUsers.and(certIdGreaterThanOrEqual, certIdLessThan),
                        builderProviders,
                        builderProviders.or(provNameA, provNameB)
                )
        );

        List<Certificate> certs = cRepo.find(conditionsBuilder);
        assertEquals(certs.size(), 2);
        //TODO finish
    }

    @Test
    public void ComplexJoinWithOneToManyNonChainRelation(){
        //Certificate providers and certificates
        Provider providerA = new Provider(1, "Provider 1", true);
        Provider providerB = new Provider(2, "Provider 2", false);

        Date certDateA = DateUtils.getUtc("2016-06-12 14:54:15");
        Certificate certA = new Certificate(1, "BHP", certDateA);
        certA.setProvider(providerA);

        Date certDateB = DateUtils.getUtc("2014-03-12 11:11:15");
        Certificate certB = new Certificate(2, "Java Cert", certDateB);
        certB.setProvider(providerB);

        //prepare users
        Date date = DateUtils.getUtc("2011-06-12 08:10:15");
        User userA = new User(1, "Rose", date);
        User userB = new User(2, "Mont", date);
        User userC = new User(3, "Tom", date);
        userA.setMainCertificate(certA);
        userB.setMainCertificate(certB);

        saveUsersData(uRepo, Arrays.asList(userA, userB, userC));

        //non-chain, one to many
        //certificate --> user
        //            --> provider

        ConditionsBuilder conditionsBuilder = new ConditionsBuilder();
        ConditionsBuilder builderUsers = conditionsBuilder.getJoinConditionsBuilder("usersOfMainCert", JoinType.LEFT);
        ConditionsBuilder builderProviders = conditionsBuilder.getJoinConditionsBuilder("provider", JoinType.LEFT);


        ConditionsBuilder.Condition certIdGreaterThanOrEqual = conditionsBuilder.greaterThanOrEqualTo("id", 0);
        ConditionsBuilder.Condition certIdLessThan = conditionsBuilder.lessThan("id", 5);

        ConditionsBuilder.Condition uIdLessThan = builderUsers.lessThan("id", 15);
        ConditionsBuilder.Condition uIdGreaterThanOrEqual = builderUsers.greaterThanOrEqualTo("id", 0);

        ConditionsBuilder.Condition provNameA = builderProviders.equal("name", "Provider 1");
        ConditionsBuilder.Condition provNameB = builderProviders.equal("name", "Provider 2");

        conditionsBuilder.or(
                conditionsBuilder.and(uIdLessThan, uIdGreaterThanOrEqual),
                null,
                conditionsBuilder.or(
                        builderUsers,
                        builderUsers.and(certIdGreaterThanOrEqual, certIdLessThan),
                        builderProviders,
                        builderProviders.or(provNameA, provNameB)
                )
        );

        List<Certificate> certs = cRepo.find(conditionsBuilder);
        assertEquals(certs.size(), 2);
        //TODO finish
    }

    @Test
    public void criteriaTODO(){
        //Certificate providers and certificates
        Provider providerA = new Provider(1, "Provider 1", true);
        Provider providerB = new Provider(2, "Provider 2", false);

        Date certDateA = DateUtils.getUtc("2016-06-12 14:54:15");
        Certificate certA = new Certificate(1, "BHP", certDateA);
        certA.setProvider(providerA);

        Date certDateB = DateUtils.getUtc("2014-03-12 11:11:15");
        Certificate certB = new Certificate(2, "Java Cert", certDateB);
        certB.setProvider(providerB);

        //prepare users
        Date date = DateUtils.getUtc("2011-06-12 08:10:15");
        User userA = new User(1, "Rose", date);
        User userB = new User(2, "Mont", date);
        User userC = new User(3, "Tom", date);
        userA.setMainCertificate(certA);
        userB.setMainCertificate(certB);

        saveUsersData(uRepo, Arrays.asList(userA, userB, userC));
        List<Predicate> predicates = new ArrayList<>();

        //select Users
        CriteriaBuilder cb = uRepo.getEntityManager().getCriteriaBuilder();
        CriteriaQuery<User> cq = cb.createQuery(User.class);
        Root<User> rootUser = cq.from(User.class);
        predicates.add(cb.lessThan(rootUser.get("id"), 15));

        //join to Cert
        Join<User, Certificate> certJoin = rootUser.join("mainCertificate");
        predicates.add(cb.lessThan(certJoin.get("id"), 14));

        //join to provider
        Join<Certificate, Provider> providerJoin = certJoin.join("provider");
        predicates.add(cb.lessThan(providerJoin.get("id"), 12));

        cq.select(rootUser);

        cq.where(cb.and((Predicate[])predicates.toArray(new Predicate[predicates.size()])));
        TypedQuery<User> typedQuery = uRepo.getEntityManager().createQuery(cq);
        List<User> users = typedQuery.getResultList();
        assertEquals(2, users.size());





//        ConditionsBuilder conditionsBuilder = new ConditionsBuilder();
//        ConditionsBuilder builderUsers = conditionsBuilder.getJoinConditionsBuilder("usersOfMainCert", JoinType.LEFT);
//        ConditionsBuilder builderProviders = conditionsBuilder.getJoinConditionsBuilder("provider", JoinType.LEFT);
//
//
//        ConditionsBuilder.Condition certIdGreaterThanOrEqual = conditionsBuilder.greaterThanOrEqualTo("id", 0);
//        ConditionsBuilder.Condition certIdLessThan = conditionsBuilder.lessThan("id", 5);
//
//        ConditionsBuilder.Condition uIdLessThan = builderUsers.lessThan("id", 15);
//        ConditionsBuilder.Condition uIdGreaterThanOrEqual = builderUsers.greaterThanOrEqualTo("id", 0);
//
//        ConditionsBuilder.Condition provNameA = builderProviders.equal("name", "Provider 1");
//        ConditionsBuilder.Condition provNameB = builderProviders.equal("name", "Provider 2");
//
//        conditionsBuilder.or(
//                conditionsBuilder.and(uIdLessThan, uIdGreaterThanOrEqual),
//                null,
//                conditionsBuilder.or(
//                        builderUsers,
//                        builderUsers.and(certIdGreaterThanOrEqual, certIdLessThan),
//                        builderProviders,
//                        builderProviders.or(provNameA, provNameB)
//                )
//        );

//        List<Certificate> certs = cRepo.find(conditionsBuilder);
//        assertEquals(certs.size(), 2);
        //TODO finish
    }
}
