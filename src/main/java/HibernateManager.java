import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.util.List;

public class HibernateManager implements AutoCloseable {
    Configuration configuration;
    SessionFactory factory;
    Session session;

    HibernateManager(){
        configuration = new Configuration().configure();
        factory = configuration.buildSessionFactory();
        session = factory.getCurrentSession();
        session.beginTransaction();
    }

    public  List<User> getData(){
        List<User> users = session.createCriteria(User.class).list();
        session.getTransaction().commit();
        return users;
    }

    public void writeUserData(User user){
        session.save(user);
    }

    @Override
    public void close() throws Exception {
        //no necessity to close session, because getCurrentSession() was used
    }
}
