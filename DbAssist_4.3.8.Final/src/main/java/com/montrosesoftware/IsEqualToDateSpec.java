package com.montrosesoftware;

import com.montrosesoftware.entities.User;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.Date;

public class IsEqualToDateSpec implements Specification<User> {

    private Date utcDate;

    public IsEqualToDateSpec(){}

    public IsEqualToDateSpec (Date utcDate){
        this.utcDate = utcDate;
    }

    @Override
    public Predicate toPredicate(Root<User> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        return cb.equal(root.get("createdAt"), utcDate);
        //return cb.equal(root.get("createdAt"), getExpression(cb, utcDate, Date.class));
    }
}
