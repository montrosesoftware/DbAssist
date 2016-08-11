package com.montrosesoftware.repositories;

import com.montrosesoftware.DateUtils;
import com.montrosesoftware.entities.Certificate;
import com.montrosesoftware.repositories.AbstractRepository;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

@Repository
public class CertificateRepo extends AbstractRepository<Certificate> {

    public CertificateRepo() {
        super(Certificate.class);
    }

    @PersistenceContext
    protected EntityManager entityManager;

    public Certificate get(int id){
            return entityManager.find(Certificate.class, id);
        }

    public void saveAsPlainSQL(Certificate cert){
        String sql = "INSERT INTO jpa.certificates (id, name, expiration_date) VALUES (" + cert.getId() +", '" + cert.getName() + "', '" + DateUtils.getUtc(cert.getExpirationDate()) + "')";
        Query query = entityManager.createNativeQuery(sql);
        query.executeUpdate();
    }
}
