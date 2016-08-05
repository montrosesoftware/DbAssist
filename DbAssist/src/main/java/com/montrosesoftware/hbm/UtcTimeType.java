package com.montrosesoftware.hbm;

import org.hibernate.type.TimeType;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class UtcTimeType extends TimeType {

    private static final long serialVersionUID = 8077663283676934635L;
       private static final TimeZone UTC = TimeZone.getTimeZone("UTC");

    @Override
    public Object get(ResultSet rs, String name) throws SQLException {
       return rs.getTime(name, Calendar.getInstance(UTC)); 
    }

    @Override
    public void set(PreparedStatement st, Object value, int index) throws SQLException {
       Time time;
        if(value instanceof Time) {
               time = (Time) value;
        } else {
               time = new Time(((Date) value).getTime());
        }
        st.setTime(index, time, Calendar.getInstance(UTC));
    }
}