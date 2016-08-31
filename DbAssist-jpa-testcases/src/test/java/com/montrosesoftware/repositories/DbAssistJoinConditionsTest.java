package com.montrosesoftware.repositories;

import com.montrosesoftware.config.BaseTest;
import com.montrosesoftware.entities.Certificate;
import com.montrosesoftware.entities.Provider;
import com.montrosesoftware.entities.User;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import static com.montrosesoftware.helpers.TestUtils.prepareAndSaveExampleDataToDb;
import static com.montrosesoftware.repositories.ConditionsBuilder.and;
import static com.montrosesoftware.repositories.ConditionsBuilder.or;
import static org.junit.Assert.*;


public class DbAssistJoinConditionsTest extends BaseTest {

    @Autowired
    private UserRepo uRepo;

    @Autowired
    private CertificateRepo cRepo;

    @Autowired
    private ProviderRepo pRepo;

    @Test
    public void writeAndReadUsersOneToManyRelation() {
        prepareAndSaveExampleDataToDb(uRepo);

        List<User> results = uRepo.find(new ConditionsBuilder());
        assertEquals(3, results.size());
    }

    @Test
    public void joinTestChainOfThreeChainedEntities() {
        prepareAndSaveExampleDataToDb(uRepo);

        //handle joins
        ConditionsBuilder builderUsers = new ConditionsBuilder();
        ConditionsBuilder builderCertificates = builderUsers.join("certificates", JoinType.LEFT);
        ConditionsBuilder builderProviders = builderCertificates.join("provider", JoinType.LEFT);

        HierarchyCondition conUserIdLessThan = builderUsers.lessThan("id", 3);
        HierarchyCondition conProvName = builderProviders.equal("name", "Provider 1");
        HierarchyCondition hc = or(
                conProvName,
                conUserIdLessThan
        );
        builderUsers.apply(hc);

        // ... WHERE provider.name = ? OR user.id < ?
        List<User> usersReadMultipleJoin = uRepo.find(builderUsers);
        assertEquals(usersReadMultipleJoin.size(), 2);
    }

    @Test
    public void joinTestChainOfFourChainedEntities() {
        prepareAndSaveExampleDataToDb(uRepo);

        ConditionsBuilder builderUsers = new ConditionsBuilder();
        ConditionsBuilder builderCertificates = builderUsers.join("certificates", JoinType.LEFT);
        ConditionsBuilder builderProviders = builderCertificates.join("provider", JoinType.LEFT);
        ConditionsBuilder builderCountries = builderProviders.join("country", JoinType.LEFT);

        //conditions from 1st to 4th entity
        HierarchyCondition conUserIdLessThan = builderUsers.lessThan("id", 15);
        HierarchyCondition conProvName = builderProviders.equal("name", "Provider 1");
        HierarchyCondition conCountryIdLessThan = builderCountries.lessThan("id", 25);

        HierarchyCondition hc = and(or(conUserIdLessThan, conCountryIdLessThan), conProvName);
        builderUsers.apply(hc);

        // ... WHERE (user.id < ? OR country.id < ?) AND provider.name = ?
        List<User> usersReadMultipleJoin = uRepo.find(builderUsers);
        assertEquals(usersReadMultipleJoin.size(), 1);
    }

    @Test
    public void ComplexJoinWithOneToManyChainRelation() {
        prepareAndSaveExampleDataToDb(uRepo);

        // chain, one to many
        // user - certificate - provider - country
        ConditionsBuilder cb = new ConditionsBuilder();
        ConditionsBuilder builderCertificate = cb.join("mainCertificate", JoinType.LEFT);
        ConditionsBuilder builderProvider = builderCertificate.join("provider", JoinType.LEFT);
        ConditionsBuilder builderCountry = builderProvider.join("country", JoinType.LEFT);

        HierarchyCondition conUserIdGreaterThanOrEqual = cb.greaterThanOrEqualTo("id", 0);
        HierarchyCondition conUserIdLessThan = cb.lessThan("id", 3);
        HierarchyCondition conCertIdLessThan = builderCertificate.lessThan("id", 5);
        HierarchyCondition conCertIdGreaterThanOrEqual = builderCertificate.greaterThanOrEqualTo("id", 0);
        HierarchyCondition conProvName = builderProvider.equal("name", "Provider 1");
        HierarchyCondition conCountryName = builderCountry.equal("name", "USA");

        HierarchyCondition hc = or(
                and(conUserIdLessThan, conUserIdGreaterThanOrEqual),
                or(
                        and(conCertIdGreaterThanOrEqual, conCertIdLessThan),
                        or(conProvName, conCountryName)
                )
        );
        cb.apply(hc);

        // ... WHERE (user.id < ? AND user.id >= ?) OR (certificate.id >= ? AND certificate.id < ?) OR provider.name = ? OR country.name = ?
        List<User> results = uRepo.find(cb);
        assertEquals(results.size(), 2);
    }

    @Test
    public void ComplexJoinWithOneToManyNonChainRelation() {
        prepareAndSaveExampleDataToDb(uRepo);

        // non-chain, one to many
        // certificate - user
        //             - provider - country

        ConditionsBuilder cb = new ConditionsBuilder();
        ConditionsBuilder builderUsers = cb.join("usersOfMainCert", JoinType.LEFT);
        ConditionsBuilder builderProviders = cb.join("provider", JoinType.LEFT);
        ConditionsBuilder builderCountries = builderProviders.join("country", JoinType.LEFT);

        HierarchyCondition conCertIdGreaterThanOrEqual = cb.greaterThanOrEqualTo("id", 0);
        HierarchyCondition conCertIdLessThan = cb.lessThan("id", 2);
        HierarchyCondition conUserIdLessThan = builderUsers.lessThan("id", 3);
        HierarchyCondition conUserIdGreaterThanOrEqual = builderUsers.greaterThanOrEqualTo("id", 1);
        HierarchyCondition conProvNameA = builderProviders.equal("name", "Provider 1");
        HierarchyCondition conCountryName = builderCountries.equal("name", "USA");

        HierarchyCondition hc = or(
                and(conUserIdLessThan, conUserIdGreaterThanOrEqual),
                or(
                        and(conCertIdGreaterThanOrEqual, conCertIdLessThan),
                        or(conProvNameA, conCountryName)
                )
        );
        cb.apply(hc);

        // ... WHERE (user.id < ? AND user.id >= ?) OR (certificate.id >= ? AND certificate.id < ?) OR provider.name = ? OR country.name = ?
        List<Certificate> certs = cRepo.find(cb);
        assertEquals(certs.size(), 2);
        //TODO think of another conditions, which return only one entity
    }

    @Test
    public void ComplexJoinWithManyToManyChainRelation() {
        prepareAndSaveExampleDataToDb(uRepo);

        // chain, many to many
        // user - certificate - provider - country

        ConditionsBuilder cb = new ConditionsBuilder();
        ConditionsBuilder builderCerts = cb.join("certificates", JoinType.LEFT);
        ConditionsBuilder builderProvider = builderCerts.join("provider", JoinType.LEFT);
        ConditionsBuilder builderCountry = builderProvider.join("country", JoinType.LEFT);

        HierarchyCondition conUserIdGreaterThanOrEqual = cb.greaterThanOrEqualTo("id", 0);
        HierarchyCondition conUserIdLessThan = cb.lessThan("id", 3);
        HierarchyCondition conCertIdLessThan = builderCerts.lessThan("id", 5);
        HierarchyCondition conCertIdGreaterThanOrEqual = builderCerts.greaterThanOrEqualTo("id", 0);
        HierarchyCondition conProvName = builderProvider.equal("name", "Provider 1");
        HierarchyCondition conCountryName = builderCountry.equal("name", "USA");

        HierarchyCondition hc = or(
                cb.and(conUserIdLessThan, conUserIdGreaterThanOrEqual),
                or(
                        cb.and(conCertIdGreaterThanOrEqual, conCertIdLessThan),
                        or(conProvName, conCountryName)
                )
        );
        cb.apply(hc);

// ... WHERE (user.id < ? AND user.id >= ?) OR (certificate.id >= ? AND certificate.id < ?) OR provider.name = ? OR country.name = ?
        List<User> users = uRepo.find(cb);
        assertEquals(users.size(), 2);
    }

    @Test
    public void ComplexJoinWithManyToManyNonChainRelation() {
        prepareAndSaveExampleDataToDb(uRepo);

        // non-chain, many to many
        // certificate - user
        //             - provider - country

        ConditionsBuilder cb = new ConditionsBuilder();
        ConditionsBuilder builderUsers = cb.join("users", JoinType.LEFT);
        ConditionsBuilder builderProviders = cb.join("provider", JoinType.LEFT);
        ConditionsBuilder builderCountries = builderProviders.join("country", JoinType.LEFT);

        HierarchyCondition conCertIdGreaterThanOrEqual = cb.greaterThanOrEqualTo("id", 0);
        HierarchyCondition conCertIdLessThan = cb.lessThan("id", 2);
        HierarchyCondition conUserIdLessThan = builderUsers.lessThan("id", 15);
        HierarchyCondition conUserIdGreaterThanOrEqual = builderUsers.greaterThanOrEqualTo("id", 0);
        HierarchyCondition conProvName = builderProviders.equal("name", "Provider 1");
        HierarchyCondition conCountryName = builderCountries.equal("name", "USA");

        HierarchyCondition hc = or(
                and(conUserIdLessThan, conUserIdGreaterThanOrEqual),
                or(
                        and(conCertIdGreaterThanOrEqual, conCertIdLessThan),
                        or(conProvName, conCountryName)
                )
        );
        cb.apply(hc);

        // ... WHERE (user.id < ? AND user.id >= ?) OR (certificate.id >= ? AND certificate.id < ?) OR provider.name = ? OR country.name = ?
        List<Certificate> certificates = cRepo.find(cb);
        assertEquals(certificates.size(), 2);
        //TODO think of another conditions, which return only one entity
    }

    @Test
    public void joinOnThreeEntitiesChainUsingCriteriaBuilder() {
        prepareAndSaveExampleDataToDb(uRepo);

        List<Predicate> predicates = new ArrayList<>();

        CriteriaBuilder cb = uRepo.getEntityManager().getCriteriaBuilder();
        CriteriaQuery<User> cq = cb.createQuery(User.class);

        //handle join
        Root<User> rootUser = cq.from(User.class);
        Join<User, Certificate> certJoin = rootUser.join("mainCertificate");
        Join<Certificate, Provider> providerJoin = certJoin.join("provider");

        //add conditions
        predicates.add(cb.lessThan(rootUser.get("id"), 15));
        predicates.add(cb.lessThan(providerJoin.get("id"), 12));

        cq.select(rootUser);
        cq.where(cb.and((Predicate[]) predicates.toArray(new Predicate[predicates.size()])));
        TypedQuery<User> typedQuery = uRepo.getEntityManager().createQuery(cq);

        // ... WHERE user.id < ? AND provider.id < ?
        List<User> users = typedQuery.getResultList();
        assertEquals(2, users.size());
    }

    @Test
    public void joinAndFetchTestOnFourEntitiesChain() {
        prepareAndSaveExampleDataToDb(uRepo);

        ConditionsBuilder builderUsers = new ConditionsBuilder();
        ConditionsBuilder builderCertificates = builderUsers.join("certificates", JoinType.LEFT);

        //conditions on first two entities
        HierarchyCondition conUserIdLessThan = builderUsers.lessThan("id", 15);
        HierarchyCondition conCertName = builderCertificates.equal("name", "BHP");
        HierarchyCondition hc = or(conUserIdLessThan, conCertName);
        builderUsers.apply(hc);

        //fetches:
        FetchesBuilder fetchesBuilder = new FetchesBuilder();

        fetchesBuilder
                .fetch("certificates", JoinType.LEFT);

        fetchesBuilder
                .fetch("certificates", JoinType.LEFT)
                .fetch("provider", JoinType.LEFT)
                .fetch("country", JoinType.LEFT);

        List<User> usersReadMultipleJoin = uRepo.find(builderUsers, fetchesBuilder);
        assertEquals(usersReadMultipleJoin.size(), 3);

        User userA = usersReadMultipleJoin.get(0);
        assertNotNull(userA);
    }

    @Test
    public void forkedFetchTest() {
        prepareAndSaveExampleDataToDb(uRepo);

        ConditionsBuilder builderUsers = new ConditionsBuilder();

        FetchesBuilder fetchesBuilder = new FetchesBuilder();
        fetchesBuilder
                .fetch("certificates", JoinType.LEFT)
                .fetch("usersOfMainCert", JoinType.LEFT);

        fetchesBuilder
                .fetch("certificates", JoinType.LEFT)
                .fetch("provider", JoinType.LEFT);

        List<User> users = uRepo.find(builderUsers, fetchesBuilder);
        //TODO close transaction try to access
        assertEquals(users.size(), 3);
    }

    @Test
    public void orderByJoinedAttribute() {
        prepareAndSaveExampleDataToDb(uRepo);

        //joins
        ConditionsBuilder rootBuilder = new ConditionsBuilder();
        ConditionsBuilder builderCerts = rootBuilder.join("certificates", JoinType.LEFT);

        //sorting
        OrderBy orderBy = new OrderBy();
        orderBy
                .asc(rootBuilder, "name")
                .desc(rootBuilder, "id")
                .asc(builderCerts, "id")
                .desc(rootBuilder, "createdAt");

        List<User> results = uRepo.find(rootBuilder, orderBy);

        assertEquals(results.size(), 3);
        assertTrue(results.get(0).getId() == 2);
        assertTrue(results.get(1).getId() == 1);
        assertTrue(results.get(2).getId() == 3);
    }

    @Test
    public void orderByJoinedAttributeUsingJPA() {
        prepareAndSaveExampleDataToDb(uRepo);

        EntityManager em = uRepo.getEntityManager();
        CriteriaBuilder cb = em.getCriteriaBuilder();

        CriteriaQuery<User> cq = cb.createQuery(User.class);
        Root<User> root = cq.from(User.class);
        Join<User, Certificate> joinCert = root.join("certificates");

        cq.select(root);
        List<Order> orderList = new ArrayList<>();
        orderList.add(cb.asc(root.get("name")));
        orderList.add(cb.desc(root.get("id")));
        orderList.add(cb.asc(joinCert.get("id")));
        orderList.add(cb.desc(root.get("createdAt")));

        cq.orderBy(orderList);

        TypedQuery<User> q = em.createQuery(cq);
        List<User> results = new ArrayList(new LinkedHashSet( q.getResultList()));

        assertEquals(results.size(), 3);
        assertTrue(results.get(0).getId() == 2);
        assertTrue(results.get(1).getId() == 1);
        assertTrue(results.get(2).getId() == 3);
        //TODO verify why it fails
    }
}


