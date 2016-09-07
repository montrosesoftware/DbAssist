package com.montrosesoftware.dbassist.repositories;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Order;

public class OrderBy extends BaseJoinPairsList<BaseJoinPair<Order>, Order> {

    public OrderBy asc(ConditionsBuilder join, String attributeName) {
        list.add(new BaseJoinPair<Order>(join, attributeName) {

            @Override
            public Order apply(CriteriaBuilder criteriaBuilder, ConditionsBuilder rootBuilder, From root) {
                return criteriaBuilder.asc(rootBuilder.getFrom(root, this.getJoinBuilder()).get(this.getAttributeName()));
            }
        });
        return this;
    }

    public OrderBy desc(ConditionsBuilder join, String attributeName) {
        list.add(new BaseJoinPair<Order>(join, attributeName) {

            @Override
            public Order apply(CriteriaBuilder criteriaBuilder, ConditionsBuilder rootBuilder, From root) {
                return criteriaBuilder.desc(rootBuilder.getFrom(root, this.getJoinBuilder()).get(this.getAttributeName()));
            }
        });
        return this;
    }
}
