package com.montrosesoftware.repositories;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Order;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class OrderBy {

    private ConditionsBuilder joinBuilder;
    private OrderByI applicableOrderBy;

    public OrderBy(ConditionsBuilder joinBuilder, LinkedHashMap<String, OrderType> orders) {
        this.joinBuilder = joinBuilder;
        this.applicableOrderBy = (builder, root) -> {
            List<Order> list = new ArrayList<>();
            for (Map.Entry<String, OrderType> entry : orders.entrySet()) {
                if (entry.getValue() == OrderType.ASC)
                    list.add(builder.asc(root.get(entry.getKey())));
                else
                    list.add(builder.desc(root.get(entry.getKey())));
            }
            return list;
        };
    }

    public OrderByI getApplicableOrderBy() {
        return applicableOrderBy;
    }

    public ConditionsBuilder getJoinBuilder() {
        return joinBuilder;
    }

    public enum OrderType {
        ASC,
        DESC
    }

    protected interface OrderByI {
        List<Order> apply(CriteriaBuilder criteriaBuilder, From<?, ?> root);
    }

    public class SingleOrder {
        private ConditionsBuilder joinBuilder;
        private String attributeName;
        private OrderType orderType;

        public SingleOrder(ConditionsBuilder joinBuilder, String attributeName, OrderType orderType) {
            this.joinBuilder = joinBuilder;
            this.attributeName = attributeName;
            this.orderType = orderType;
        }

        public ConditionsBuilder getJoinBuilder() {
            return joinBuilder;
        }

        public String getAttributeName() {
            return attributeName;
        }

        public OrderType getOrderType() {
            return orderType;
        }
    }
}
