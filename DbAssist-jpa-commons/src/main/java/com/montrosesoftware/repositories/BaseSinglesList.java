package com.montrosesoftware.repositories;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.From;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseSinglesList<T extends BaseJoinPair, S> {

    protected List<T> list = new ArrayList<>();

    public BaseSinglesList() {
    }

    public abstract List<S> getAll(CriteriaBuilder criteriaBuilder, ConditionsBuilder rootBuilder, From<?, ?> root);
}
