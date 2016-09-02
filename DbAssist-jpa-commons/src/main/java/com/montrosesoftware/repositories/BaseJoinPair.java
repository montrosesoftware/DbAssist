package com.montrosesoftware.repositories;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.From;

public abstract class BaseJoinPair<T> {
    protected ConditionsBuilder joinBuilder;
    protected String attributeName;

    public BaseJoinPair(ConditionsBuilder joinBuilder, String attributeName) {
        this.joinBuilder = joinBuilder;
        this.attributeName = attributeName;
    }

    public ConditionsBuilder getJoinBuilder() {
        return joinBuilder;
    }

    public String getAttributeName() {
        return attributeName;
    }

    public abstract T apply(CriteriaBuilder criteriaBuilder, ConditionsBuilder rootBuilder, From<?, ?> root);
}

