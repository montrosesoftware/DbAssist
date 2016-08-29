package com.montrosesoftware;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class DateUtils {

    public static final TimeZone UTCTimeZone = TimeZone.getTimeZone("UTC");

    public static Date getUtc(String utc, boolean onlyDate) {
        SimpleDateFormat format = onlyDate ? getUtcDateOnlyFormat() : getUtcDateFormat();

        try {
            return format.parse(utc);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public static Date getUtc(String utc) {
        return getUtc(utc, false);
    }

    public static SimpleDateFormat getUtcDateFormat() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        format.setTimeZone(UTCTimeZone);

        return format;
    }

    public static SimpleDateFormat getUtcDateOnlyFormat() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        format.setTimeZone(UTCTimeZone);

        return format;
    }

    //TODO make generic
    public static String getUtc(Date date, boolean onlyDate) {
        SimpleDateFormat format = onlyDate ? getUtcDateOnlyFormat() : getUtcDateFormat();

        return format.format(date);
    }

    public static String getUtc(Date date) {
        return getUtc(date, false);
    }

    public static String getUtc(Timestamp timestamp, boolean onlyDate) {
        SimpleDateFormat format = onlyDate ? getUtcDateOnlyFormat() : getUtcDateFormat();
        return format.format(timestamp);
    }

    public static String getUtc(Timestamp timestamp) {
        return getUtc(timestamp, false);
    }
}
