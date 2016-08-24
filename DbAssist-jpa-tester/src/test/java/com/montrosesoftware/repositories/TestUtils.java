package com.montrosesoftware.repositories;

import com.montrosesoftware.DateUtils;
import com.montrosesoftware.entities.Certificate;
import com.montrosesoftware.entities.Country;
import com.montrosesoftware.entities.Provider;
import com.montrosesoftware.entities.User;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public class TestUtils {

    public static Date addMinutes(Date date, int minutes){
        final long OneMinuteInMs = 60000;
        long currentTimeInMs = date.getTime();
        Date newDate = new Date(currentTimeInMs + (minutes * OneMinuteInMs));
        return newDate;
    }

    public static <T> boolean collectionsAreEqual(Collection<T> a, Collection<T> b){
        return a.containsAll(b) && b.containsAll(a);
    }

    public static void saveUsersData(UserRepo uRepo, List<User> usersToSave){
        usersToSave.forEach(uRepo::save);
        uRepo.clearPersistenceContext();
    }

    public static void prepareAndSaveExampleDataToDb(UserRepo uRepo) {
        //Countries
        Country countryA = new Country(1, "Poland");
        Country countryB = new Country(2, "USA");

        //Certificate providers
        Provider providerA = new Provider(1, "Provider 1", true);
        Provider providerB = new Provider(2, "Provider 2", false);
        providerA.setCountry(countryA);
        providerB.setCountry(countryB);

        //Certificates
        Date certDateA = DateUtils.getUtc("2016-06-12 14:54:15");
        Certificate certA = new Certificate(1, "BHP", certDateA);
        certA.setProvider(providerA);

        Date certDateB = DateUtils.getUtc("2014-03-12 11:11:15");
        Certificate certB = new Certificate(2, "Java Cert", certDateB);
        certB.setProvider(providerB);

        //Users
        Date dateA = DateUtils.getUtc("2016-06-12 07:10:15");
        Date dateB = DateUtils.getUtc("2010-06-12 08:10:15");
        User userA = new User(1, "Rose", dateA);
        User userB = new User(2, "Mont", dateB);
        User userC = new User(3, "Tom", dateB);

        userA.setMainCertificate(certA);
        userB.setMainCertificate(certB);

        userA.addCertificate(certA);
        userA.addCertificate(certB);
        userB.addCertificate(certB);

        saveUsersData(uRepo, Arrays.asList(userA, userB, userC));
    }

}
