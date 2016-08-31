package com.montrosesoftware.repositories;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Order;
import java.util.ArrayList;
import java.util.List;

public class OrderBy {
    private List<SingleOrder> singleOrders = new ArrayList<>();

    public OrderBy() {
    }

    public OrderBy asc(ConditionsBuilder join, String attributeName) {
        singleOrders.add(new SingleOrder(join, attributeName, OrderType.ASC));
        return this;
    }

    public OrderBy desc(ConditionsBuilder join, String attributeName) {
        singleOrders.add(new SingleOrder(join, attributeName, OrderType.DESC));
        return this;
    }

    public List<Order> getListOfOrders(CriteriaBuilder criteriaBuilder, ConditionsBuilder rootBuilder, From<?, ?> root) {
        List<Order> orderList = new ArrayList<>();
        for (SingleOrder singleOrder : singleOrders) {
            if (singleOrder.getOrderType() == OrderType.ASC)
                orderList.add(criteriaBuilder.asc(rootBuilder.getFrom(root, singleOrder.getJoinBuilder()).get(singleOrder.getAttributeName())));
            else
                orderList.add(criteriaBuilder.desc(rootBuilder.getFrom(root, singleOrder.getJoinBuilder()).get(singleOrder.getAttributeName())));
        }
        return orderList;
    }

    public enum OrderType {
        ASC,
        DESC
    }

    private class SingleOrder {
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
