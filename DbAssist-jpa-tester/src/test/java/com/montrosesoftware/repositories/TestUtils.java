package com.montrosesoftware.repositories;

import java.util.Collection;
import java.util.Date;

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
}
