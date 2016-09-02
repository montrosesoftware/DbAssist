package com.montrosesoftware;

import org.hibernate.*;
import org.hibernate.cfg.Configuration;

import java.util.List;

public class HibernateManager implements AutoCloseable {

    private Configuration configuration;
    private SessionFactory factory;
    private Session session;
    private Transaction transaction;

    public HibernateManager(){
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
        SQLQuery query = session.createSQLQuery("SELECT name, created_at, updated_at, last_logged_at FROM hbm.users");
        List list = query.list();
        return list;
    }

    public void writeUserData(User user){
        session.save(user);
        session.flush();
    }

    public void writeUserDataByPlainSQL(User user){
        String sql = "INSERT INTO hbm.users (id, name, created_at, updated_at, last_logged_at) VALUES ("
                + user.getId() +", '"
                + user.getName() + "', '"
                + DateUtils.getUtc(user.getCreatedAt()) + "', '"
                + DateUtils.getUtc(user.getUpdatedAt()) + "', '"
                + DateUtils.getUtc(user.getLastLoggedAt())
                + "')";
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
