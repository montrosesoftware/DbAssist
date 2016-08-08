package com.montrosesoftware;

import com.montrosesoftware.DateUtils;
import org.hibernate.*;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.cfg.Configuration;

import java.util.List;

public class HibernateManagerAnnot implements AutoCloseable {
    private Configuration configuration;
    private SessionFactory factory;
    private Session session;
    private Transaction transaction;

    public HibernateManagerAnnot(){
        configuration = new AnnotationConfiguration().configure();
        factory = configuration.buildSessionFactory();
        session = factory.getCurrentSession();
        session.beginTransaction();
        transaction = session.getTransaction();
    }

    public List<UserAnnot> getData(){
        return session.createCriteria(UserAnnot.class).list();
    }

    public List<Object[]> getDataByPlainSQL(){
        SQLQuery query = session.createSQLQuery("SELECT NAME, CREATEDAT FROM USERS");
        List list = query.list();
        return list;
    }

    public void writeUserData(UserAnnot user){
        session.save(user);
        session.flush();
    }

    public void writeUserDataByPlainSQL(UserAnnot user){
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

