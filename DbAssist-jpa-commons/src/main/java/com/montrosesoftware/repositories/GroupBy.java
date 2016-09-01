package com.montrosesoftware.repositories;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;
import java.util.ArrayList;
import java.util.List;

public class GroupBy extends BaseSinglesList<GroupBy.SingleGroup, Expression<?>> {

    public GroupBy groupBy(ConditionsBuilder joinBuilder, String attributeName) {
        list.add(new SingleGroup(joinBuilder, attributeName));
        return this;
    }

    @Override
    public List<Expression<?>> getAll(CriteriaBuilder criteriaBuilder, ConditionsBuilder rootBuilder, From<?, ?> root) {
        List<Expression<?>> expressions = new ArrayList<>();
        for (SingleGroup singleGroup : list) {
            expressions.add(rootBuilder.getFrom(root, singleGroup.getJoinBuilder()).get(singleGroup.getAttributeName()));
        }
        return expressions;
    }

    protected class SingleGroup extends BaseJoinPair {

        public SingleGroup(ConditionsBuilder joinBuilder, String attributeName) {
            super(joinBuilder, attributeName);
        }
    }
}
