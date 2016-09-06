package com.montrosesoftware.types;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.hibernate.type.TimestampType;

/**
 * The class overrides appropriate methods of Hibernate's TimestampType
 * so that the dates are treated as UTC dates when writing to/reading from the DB
 */
public class UtcDateType extends TimestampType {

    private static final TimeZone UTC = TimeZone.getTimeZone("UTC");

    @Override
    public Object get(ResultSet rs, String name) throws SQLException {
        return rs.getTimestamp(name, Calendar.getInstance(UTC));
    }

    @Override
    public void set(PreparedStatement st, Object value, int index) throws SQLException {
        Timestamp ts;
        if(value instanceof Timestamp) {
            ts = (Timestamp) value;
        } else {
            ts = new Timestamp(((Date) value).getTime());
        }
        st.setTimestamp(index, ts, Calendar.getInstance(UTC));
    }
}