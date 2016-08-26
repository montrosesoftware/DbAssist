package com.montrosesoftware.repositories;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;

public interface ApplicableCondition {
    Predicate apply(CriteriaBuilder cb, From<?, ?> root);
}
