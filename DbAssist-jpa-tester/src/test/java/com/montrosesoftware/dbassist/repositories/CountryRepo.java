package com.montrosesoftware.dbassist.repositories;

import com.montrosesoftware.dbassist.entities.Country;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Repository
public class CountryRepo extends AbstractRepository<Country> {

    public CountryRepo() {
        super(Country.class);
    }

    @PersistenceContext
    protected EntityManager entityManager;

    public Country get(int id) {
        return entityManager.find(Country.class, id);
    }

    public void save(Country country){
        entityManager.persist(country);
        entityManager.flush();
    }
}
