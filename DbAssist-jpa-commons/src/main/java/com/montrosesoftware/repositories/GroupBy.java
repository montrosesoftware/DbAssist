package com.montrosesoftware.repositories;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;

public class GroupBy extends BaseJoinPairsList<BaseJoinPair<Expression<?>>, Expression<?>> {

    public GroupBy groupBy(ConditionsBuilder joinBuilder, String attributeName) {
        list.add(new BaseJoinPair<Expression<?>>(joinBuilder, attributeName) {
            @Override
            public Expression<?> apply(CriteriaBuilder criteriaBuilder, ConditionsBuilder rootBuilder, From root) {
                return rootBuilder.getFrom(root, this.getJoinBuilder()).get(this.getAttributeName());
            }
        });
        return this;
    }
}
