package com.montrosesoftware.repositories;

import com.montrosesoftware.DateUtils;
import com.montrosesoftware.entities.User;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.Date;
import java.util.List;

@Repository
public class UserRepo extends AbstractRepository<User> {

    public UserRepo() {
        super(User.class);
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    @PersistenceContext
    protected EntityManager entityManager;

    public User get(int id){
        return entityManager.find(User.class, id);
    }

    public List getDataByPlainSQL(){
        String sql = "SELECT name, created_at, updated_at, last_logged_at FROM jpa.users";
        Query query = entityManager.createNativeQuery(sql);
        List users = query.getResultList();
        return users;
    }

    public void save(User user){
        entityManager.persist(user);
        entityManager.flush();
    }

    public void clearPersistenceContext(){
        entityManager.clear();
    }

    public void saveAsPlainSQL(User user){
        String sql = "INSERT INTO jpa.users (id, name, created_at, updated_at, last_logged_at) VALUES ("
                + user.getId() +", '"
                + user.getName() + "', '"
                + DateUtils.getUtc(user.getCreatedAt()) + "', '"
                + DateUtils.getUtc(user.getUpdatedAt()) + "', '"
                + DateUtils.getUtc(user.getLastLoggedAt()) + "')";
        Query query = entityManager.createNativeQuery(sql);
        query.executeUpdate();
    }

    public User getUsingCriteria(){
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<User> criteriaQuery = criteriaBuilder.createQuery(User.class);
        Root<User> root = criteriaQuery.from(User.class);

        Predicate p = criteriaBuilder.equal(root.get("id"), 1);
        criteriaQuery.where(p);

        TypedQuery<User> typedQuery = entityManager.createQuery(criteriaQuery.select(root));
        List<User> results = typedQuery.getResultList();
        return results.isEmpty() ? null : results.get(0);
    }

    public User getUsingSpecification(Date utcDate){
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<User> criteriaQuery = criteriaBuilder.createQuery(User.class);
        Root<User> userRoot = criteriaQuery.from(User.class);

        String paramName = "pn";
        Specification<User> specs = (root, query, cb) ->
                cb.equal(root.get("createdAt"), cb.parameter(Date.class, paramName));

        Predicate predicate = specs.toPredicate(userRoot,criteriaQuery, criteriaBuilder);
        criteriaQuery.where(predicate);

        TypedQuery<User> typedQuery = entityManager.createQuery(criteriaQuery);
        typedQuery.setParameter(paramName, utcDate);

        List<User> results = typedQuery.getResultList();
        return results.isEmpty() ? null : results.get(0);
    }

    public User getUsingConditionsBuilder(Date utcDate){

        ConditionsBuilder conditionsBuilder = new ConditionsBuilder();
        conditionsBuilder.equal("createdAt", utcDate);

        List<User> results = find(conditionsBuilder, null, null);
        return results.isEmpty() ? null : results.get(0);
    }
}
