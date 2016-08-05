package com.montrosesoftware.hbm;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.hibernate.type.DateType;

public class UtcDateType extends DateType {

    private static final long serialVersionUID = 7078663283647934682L;
    private static final TimeZone UTC = TimeZone.getTimeZone("UTC");

    @Override
    public Object get(ResultSet rs, String name) throws SQLException {
        return rs.getDate(name, Calendar.getInstance(UTC));
    }

    @Override
    public void set(PreparedStatement st, Object value, int index) throws SQLException {
        java.sql.Date sqlDate;
        if(value instanceof java.sql.Date) {
            sqlDate = (java.sql.Date) value;
        } else {
            sqlDate = new java.sql.Date(((Date) value).getTime());
        }
        st.setDate(index, sqlDate, Calendar.getInstance(UTC));
    }
}