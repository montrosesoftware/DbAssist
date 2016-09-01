package com.montrosesoftware.repositories;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;
import java.util.ArrayList;
import java.util.List;

public class GroupBy {

    private List<SingleGroup> singleGroups = new ArrayList<>();

    public GroupBy() {
    }

    public GroupBy groupBy(ConditionsBuilder joinBuilder, String attributeName) {
        singleGroups.add(new SingleGroup(joinBuilder, attributeName));
        return this;
    }

    public List<Expression<?>> getAll(ConditionsBuilder rootBuilder, From<?, ?> root) {
        List<Expression<?>> expressions = new ArrayList<>();
        for (SingleGroup singleGroup : singleGroups) {
            expressions.add(rootBuilder.getFrom(root, singleGroup.getJoinBuilder()).get(singleGroup.getAttributeName()));
        }
        return expressions;
    }

    private class SingleGroup extends BaseJoinPair {

        public SingleGroup(ConditionsBuilder joinBuilder, String attributeName) {
            super(joinBuilder, attributeName);
        }
    }
}
