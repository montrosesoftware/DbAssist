import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.util.List;

public class HibernateManager {

    public  List<User> getData(){

        Configuration configuration = new Configuration().configure();
        SessionFactory factory = configuration.buildSessionFactory();
        Session session = factory.getCurrentSession();
        session.beginTransaction();

        List<User> users = session.createCriteria(User.class).list();

        session.getTransaction().commit();

        return users;
    }
}
