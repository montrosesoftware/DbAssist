package com.montrosesoftware.dbassist.types;

import org.hibernate.HibernateException;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.type.AbstractSingleColumnStandardBasicType;
import org.hibernate.type.LiteralType;
import org.hibernate.type.TimestampType;
import org.hibernate.type.VersionType;
import org.hibernate.type.descriptor.ValueBinder;
import org.hibernate.type.descriptor.ValueExtractor;
import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.JavaTypeDescriptor;
import org.hibernate.type.descriptor.java.JdbcTimestampTypeDescriptor;
import org.hibernate.type.descriptor.sql.BasicBinder;
import org.hibernate.type.descriptor.sql.BasicExtractor;
import org.hibernate.type.descriptor.sql.TimestampTypeDescriptor;

import java.sql.*;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.TimeZone;

/**
 * The class overrides appropriate methods of Hibernate's AbstractSingleColumnStandardBasicType
 * so that the dates are treated as UTC dates when writing to/reading from the DB
 */
public class UtcDateType extends AbstractSingleColumnStandardBasicType<Date> implements VersionType<Date>, LiteralType<Date> {

    public UtcDateType() {
        super(UtcTimestampTypeDescriptor.INSTANCE, JdbcTimestampTypeDescriptor.INSTANCE);
    }

    @Override
    public String getName() {
        return TimestampType.INSTANCE.getName();
    }

    @Override
    public String[] getRegistrationKeys() {
        return TimestampType.INSTANCE.getRegistrationKeys();
    }

    @Override
    public Date seed(SharedSessionContractImplementor sharedSessionContractImplementor) {
        return TimestampType.INSTANCE.seed(sharedSessionContractImplementor);
    }

    @Override
    public Date next(Date date, SharedSessionContractImplementor sharedSessionContractImplementor) {
        return TimestampType.INSTANCE.next(date, sharedSessionContractImplementor);
    }

    @Override
    public Comparator<Date> getComparator() {
        return TimestampType.INSTANCE.getComparator();
    }

    @Override
    public String objectToSQLString(Date value, Dialect dialect) throws Exception {
        return TimestampType.INSTANCE.objectToSQLString(value, dialect);
    }

    @Override
    public Date fromStringValue(String xml) throws HibernateException {
        return TimestampType.INSTANCE.fromStringValue(xml);
    }

    public static class UtcTimestampTypeDescriptor extends TimestampTypeDescriptor {

        public static final UtcTimestampTypeDescriptor INSTANCE = new UtcTimestampTypeDescriptor();

        private static final TimeZone UTC = TimeZone.getTimeZone("UTC");

        @Override
        public <X> ValueBinder<X> getBinder(final JavaTypeDescriptor<X> javaTypeDescriptor) {
            return new BasicBinder<X>(javaTypeDescriptor, this) {

                @Override
                protected void doBind(PreparedStatement st, X value, int index, WrapperOptions options) throws SQLException {
                    st.setTimestamp(index, javaTypeDescriptor.unwrap(value, Timestamp.class, options), Calendar.getInstance(UTC));
                }

                @Override
                protected void doBind(CallableStatement callableStatement, X value, String paramName, WrapperOptions wrapperOptions) throws SQLException {
                    callableStatement.setTimestamp(paramName, javaTypeDescriptor.unwrap(value, Timestamp.class, wrapperOptions), Calendar.getInstance(UTC));
                }
            };
        }

        @Override
        public <X> ValueExtractor<X> getExtractor(final JavaTypeDescriptor<X> javaTypeDescriptor) {
            return new BasicExtractor<X>(javaTypeDescriptor, this) {

                @Override
                protected X doExtract(CallableStatement statement, String name, WrapperOptions options) throws SQLException {
                    return javaTypeDescriptor.wrap(statement.getTimestamp(name, Calendar.getInstance(UTC)), options);
                }

                @Override
                protected X doExtract(ResultSet rs, String name, WrapperOptions options) throws SQLException {
                    return javaTypeDescriptor.wrap(rs.getTimestamp(name, Calendar.getInstance(UTC)), options);
                }

                @Override
                protected X doExtract(CallableStatement statement, int index, WrapperOptions options) throws SQLException {
                    return javaTypeDescriptor.wrap(statement.getTimestamp(index, Calendar.getInstance(UTC)), options);
                }

            };
        }
    }
}
