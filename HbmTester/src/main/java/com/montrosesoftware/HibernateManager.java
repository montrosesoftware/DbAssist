package com.montrosesoftware;

import com.montrosesoftware.hbm.UtcDateType;
import org.hibernate.*;
import org.hibernate.cfg.Configuration;

import java.util.List;

public class HibernateManager implements AutoCloseable {
    private Configuration configuration;
    private SessionFactory factory;
    private Session session;
    private Transaction transaction;

    public HibernateManager(){
        UtcDateType a;
        configuration = new Configuration().configure();
        factory = configuration.buildSessionFactory();
        session = factory.getCurrentSession();
        session.beginTransaction();
        transaction = session.getTransaction();
    }

    public  List<User> getData(){
        return session.createCriteria(User.class).list();
    }

    public List<Object[]> getDataByPlainSQL(){
        SQLQuery query = session.createSQLQuery("SELECT NAME, CREATEDAT FROM USERS");
        List list = query.list();
        return list;
    }

    public void writeUserData(User user){
        session.save(user);
        session.flush();
    }

    public void writeUserDataByPlainSQL(User user){
        String sql = "INSERT INTO Users (Id, Name, CreatedAt) VALUES (" + user.getId() +", '" + user.getName() + "', '" + DateUtils.getUtc(user.getCreatedAt()) + "')";
        Query query = session.createSQLQuery(sql);
        query.executeUpdate();
    }

    public void commit(){
        if(transaction != null && transaction.isActive()){
            transaction.commit();
        }
    }

    public void rollbackTransaction(){
        if(transaction != null && transaction.isActive()){
            transaction.rollback();
        }
    }

    @Override
    public void close() throws Exception {
        //no necessity to close session, because getCurrentSession() was used
    }
}
