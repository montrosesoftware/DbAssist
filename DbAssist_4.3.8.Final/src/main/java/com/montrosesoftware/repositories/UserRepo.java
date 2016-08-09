package com.montrosesoftware.repositories;

import com.montrosesoftware.DateUtils;
import com.montrosesoftware.entities.User;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;

/**
 * Created by Montrose Software on 2016-08-09.
 */
@Repository
public class UserRepo {

    @PersistenceContext
    protected EntityManager entityManager;

    public User get(int id){
        return entityManager.find(User.class, id);
    }

    public List getDataByPlainSQL(){
        String sql = "SELECT Name, CreatedAt FROM Users";
        Query query = entityManager.createNativeQuery(sql);
        List users = query.getResultList();
        return users;
    }

    public void save(User user){
        entityManager.persist(user);
    }

    public void saveAsPlainSQL(User user){
        String sql = "INSERT INTO Users (Id, Name, CreatedAt) VALUES (" + user.getId() +", '" + user.getName() + "', '" + DateUtils.getUtc(user.getCreatedAt()) + "')";
        Query query = entityManager.createNativeQuery(sql);
        query.executeUpdate();
    }

    public List getUsingCriteria(){

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<User> criteriaQuery = criteriaBuilder.createQuery(User.class);
        Root<User> root = criteriaQuery.from(User.class);

        Predicate p = criteriaBuilder.equal(root.get("id"), 1);
        criteriaQuery.where(p);

        //criteriaQuery.select(root).where(criteriaBuilder.equal(root.));
        TypedQuery<User> typedQuery = entityManager.createQuery(criteriaQuery.select(root));

        return typedQuery.getResultList();
    }
}
