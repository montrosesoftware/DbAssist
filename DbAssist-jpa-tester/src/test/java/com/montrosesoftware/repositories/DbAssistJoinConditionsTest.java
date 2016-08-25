package com.montrosesoftware.repositories;

import com.montrosesoftware.config.BaseTest;
import com.montrosesoftware.entities.Certificate;
import com.montrosesoftware.entities.Provider;
import com.montrosesoftware.entities.User;
import com.montrosesoftware.repositories.ConditionsBuilder.Condition;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import static com.montrosesoftware.helpers.TestUtils.prepareAndSaveExampleDataToDb;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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
        ConditionsBuilder builderCertificates = builderUsers.getJoinConditionsBuilder("certificates", JoinType.LEFT);
        ConditionsBuilder builderProviders = builderCertificates.getJoinConditionsBuilder("provider", JoinType.LEFT);

        Condition conUserIdLessThan = builderUsers.lessThan("id", 15);
        Condition conProvName = builderProviders.equal("name", "Provider 1");
        builderUsers.or(
                conProvName,
                conUserIdLessThan
        );

        // ... WHERE provider.name = ? OR user.id < ?
        List<User> usersReadMultipleJoin = uRepo.find(builderUsers);
        assertEquals(usersReadMultipleJoin.size(), 3);
    }

    @Test
    public void joinTestChainOfFourChainedEntities() {
        prepareAndSaveExampleDataToDb(uRepo);

        ConditionsBuilder builderUsers = new ConditionsBuilder();
        ConditionsBuilder builderCertificates = builderUsers.getJoinConditionsBuilder("certificates", JoinType.LEFT);
        ConditionsBuilder builderProviders = builderCertificates.getJoinConditionsBuilder("provider", JoinType.LEFT);
        ConditionsBuilder builderCountries = builderProviders.getJoinConditionsBuilder("country", JoinType.LEFT);

        //conditions from 1st to 4th entity
        Condition conUserIdLessThan = builderUsers.lessThan("id", 15);
        Condition conProvName = builderProviders.equal("name", "Provider 1");
        Condition conCountryIdLessThan = builderCountries.lessThan("id", 25);
        builderUsers.or(
                conUserIdLessThan,
                conCountryIdLessThan
        );

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
        ConditionsBuilder builderCertificate = cb.getJoinConditionsBuilder("mainCertificate", JoinType.LEFT);
        ConditionsBuilder builderProvider = builderCertificate.getJoinConditionsBuilder("provider", JoinType.LEFT);
        ConditionsBuilder builderCountry = builderProvider.getJoinConditionsBuilder("country", JoinType.LEFT);

        Condition conUserIdGreaterThanOrEqual = cb.greaterThanOrEqualTo("id", 0);
        Condition conUserIdLessThan = cb.lessThan("id", 15);
        Condition conCertIdLessThan = builderCertificate.lessThan("id", 5);
        Condition conCertIdGreaterThanOrEqual = builderCertificate.greaterThanOrEqualTo("id", 0);
        Condition conProvName = builderProvider.equal("name", "Provider 1");
        Condition conCountryName = builderCountry.equal("name", "USA");

        cb.or(
                cb.and(conUserIdLessThan, conUserIdGreaterThanOrEqual),
                cb.or(
                        cb.and(conCertIdGreaterThanOrEqual, conCertIdLessThan),
                        cb.or(conProvName, conCountryName)
                )
        );

        // ... WHERE (user.id < ? AND user.id >= ?) OR (certificate.id >= ? AND certificate.id < ?) OR provider.name = ? OR country.name = ?
        List<User> results = uRepo.find(cb);
        assertEquals(results.size(), 3);
    }

    @Test
    public void ComplexJoinWithOneToManyNonChainRelation() {
        prepareAndSaveExampleDataToDb(uRepo);

        // non-chain, one to many
        // certificate - user
        //             - provider - country

        ConditionsBuilder cb = new ConditionsBuilder();
        ConditionsBuilder builderUsers = cb.getJoinConditionsBuilder("usersOfMainCert", JoinType.LEFT);
        ConditionsBuilder builderProviders = cb.getJoinConditionsBuilder("provider", JoinType.LEFT);
        ConditionsBuilder builderCountries = builderProviders.getJoinConditionsBuilder("country", JoinType.LEFT);

        Condition conCertIdGreaterThanOrEqual = cb.greaterThanOrEqualTo("id", 0);
        Condition conCertIdLessThan = cb.lessThan("id", 5);
        Condition conUserIdLessThan = builderUsers.lessThan("id", 15);
        Condition conUserIdGreaterThanOrEqual = builderUsers.greaterThanOrEqualTo("id", 0);
        Condition conProvNameA = builderProviders.equal("name", "Provider 1");
        Condition conCountryName = builderCountries.equal("name", "USA");

        cb.or(
                cb.and(conUserIdLessThan, conUserIdGreaterThanOrEqual),
                cb.or(
                        cb.and(conCertIdGreaterThanOrEqual, conCertIdLessThan),
                        cb.or(conProvNameA, conCountryName)
                )
        );

        // ... WHERE (user.id < ? AND user.id >= ?) OR (certificate.id >= ? AND certificate.id < ?) OR provider.name = ? OR country.name = ?
        List<Certificate> certs = cRepo.find(cb);
        assertEquals(certs.size(), 2);
    }

    @Test
    public void ComplexJoinWithManyToManyChainRelation() {
        prepareAndSaveExampleDataToDb(uRepo);

        // chain, many to many
        // user - certificate - provider - country

        ConditionsBuilder cb = new ConditionsBuilder();
        ConditionsBuilder builderCerts = cb.getJoinConditionsBuilder("certificates", JoinType.LEFT);
        ConditionsBuilder builderProvider = builderCerts.getJoinConditionsBuilder("provider", JoinType.LEFT);
        ConditionsBuilder builderCountry = builderProvider.getJoinConditionsBuilder("country", JoinType.LEFT);

        Condition conUserIdGreaterThanOrEqual = cb.greaterThanOrEqualTo("id", 0);
        Condition conUserIdLessThan = cb.lessThan("id", 15);
        Condition conCertIdLessThan = builderCerts.lessThan("id", 5);
        Condition conCertIdGreaterThanOrEqual = builderCerts.greaterThanOrEqualTo("id", 0);
        Condition conProvName = builderProvider.equal("name", "Provider 1");
        Condition conCountryName = builderCountry.equal("name", "USA");

        cb.or(
                cb.and(conUserIdLessThan, conUserIdGreaterThanOrEqual),
                cb.or(
                        cb.and(conCertIdGreaterThanOrEqual, conCertIdLessThan),
                        cb.or(conProvName, conCountryName)
                )
        );

// ... WHERE (user.id < ? AND user.id >= ?) OR (certificate.id >= ? AND certificate.id < ?) OR provider.name = ? OR country.name = ?
        List<User> users = uRepo.find(cb);
        assertEquals(users.size(), 3);
    }

    @Test
    public void ComplexJoinWithManyToManyNonChainRelation() {
        prepareAndSaveExampleDataToDb(uRepo);

        // non-chain, many to many
        // certificate - user
        //             - provider - country

        ConditionsBuilder cb = new ConditionsBuilder();
        ConditionsBuilder builderUsers = cb.getJoinConditionsBuilder("users", JoinType.LEFT);
        ConditionsBuilder builderProviders = cb.getJoinConditionsBuilder("provider", JoinType.LEFT);
        ConditionsBuilder builderCountries = builderProviders.getJoinConditionsBuilder("country", JoinType.LEFT);

        Condition conCertIdGreaterThanOrEqual = cb.greaterThanOrEqualTo("id", 0);
        Condition conCertIdLessThan = cb.lessThan("id", 5);
        Condition conUserIdLessThan = builderUsers.lessThan("id", 15);
        Condition conUserIdGreaterThanOrEqual = builderUsers.greaterThanOrEqualTo("id", 0);
        Condition conProvName = builderProviders.equal("name", "Provider 1");
        Condition conCountryName = builderCountries.equal("name", "USA");

        cb.or(
                cb.and(conUserIdLessThan, conUserIdGreaterThanOrEqual),
                cb.or(
                        cb.and(conCertIdGreaterThanOrEqual, conCertIdLessThan),
                        cb.or(conProvName, conCountryName)
                )
        );

        // ... WHERE (user.id < ? AND user.id >= ?) OR (certificate.id >= ? AND certificate.id < ?) OR provider.name = ? OR country.name = ?
        List<Certificate> certificates = cRepo.find(cb);
        assertEquals(certificates.size(), 2);
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
        ConditionsBuilder builderCertificates = builderUsers.getJoinConditionsBuilder("certificates", JoinType.LEFT);

        //conditions on first two entities
        Condition conUserIdLessThan = builderUsers.lessThan("id", 15);
        Condition conCertName = builderCertificates.equal("name", "BHP");
        builderUsers.or(
                conUserIdLessThan,
                conCertName
        );

        //fetches:
        FetchesBuilder fetchesBuilder = new FetchesBuilder();

        fetchesBuilder
                .fetch("certificates", JoinType.LEFT);

        fetchesBuilder
                .fetch("certificates", JoinType.LEFT)
                .fetch("provider", JoinType.LEFT)
                .fetch("country", JoinType.LEFT);

        List<User> usersReadMultipleJoin = uRepo.find(builderUsers, fetchesBuilder, null);
        assertEquals(usersReadMultipleJoin.size(), 3);

        User userA = usersReadMultipleJoin.get(0);
        assertNotNull(userA);
    }
}


