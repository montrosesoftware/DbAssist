package com.montrosesoftware.dbassist.repositories;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.From;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class BaseJoinPairsList<T extends BaseJoinPair<S>, S> {

    protected List<T> list = new ArrayList<>();

    public BaseJoinPairsList() {
    }

    public List<S> getAll(CriteriaBuilder criteriaBuilder, ConditionsBuilder rootBuilder, From<?, ?> root) {
        return list.stream().map(element -> element.apply(criteriaBuilder, rootBuilder, root)).collect(Collectors.toList());
    }
}
