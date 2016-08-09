package com.montrosesoftware.repositories;

import com.montrosesoftware.DateUtils;
import com.montrosesoftware.entities.User;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

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

    public void saveAsPlainSQL(User user){
        String sql = "INSERT INTO Users (Id, Name, CreatedAt) VALUES (" + user.getId() +", '" + user.getName() + "', '" + DateUtils.getUtc(user.getCreatedAt()) + "')";
        Query query = entityManager.createNativeQuery(sql);
        query.executeUpdate();
    }
}
