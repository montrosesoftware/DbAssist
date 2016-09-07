package com.montrosesoftware.dbassist.repositories;

import com.montrosesoftware.dbassist.entities.Provider;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Repository
public class ProviderRepo extends AbstractRepository<Provider> {

    public ProviderRepo() {
        super(Provider.class);
    }

    @PersistenceContext
    protected EntityManager entityManager;

    public Provider get(int id) {
        return entityManager.find(Provider.class, id);
    }

    public void save(Provider provider){
        entityManager.persist(provider);
        entityManager.flush();
    }
}
