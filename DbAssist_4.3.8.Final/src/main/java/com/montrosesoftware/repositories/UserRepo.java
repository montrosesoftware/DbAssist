package com.montrosesoftware.repositories;

import com.montrosesoftware.entities.User;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

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
}
