package com.montrosesoftware.repositories;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Order;
import java.util.ArrayList;
import java.util.List;

public class OrderBy extends BaseSinglesList<OrderBy.SingleOrder, Order> {

    public OrderBy asc(ConditionsBuilder join, String attributeName) {
        list.add(new SingleOrder(join, attributeName, OrderType.ASC));
        return this;
    }

    public OrderBy desc(ConditionsBuilder join, String attributeName) {
        list.add(new SingleOrder(join, attributeName, OrderType.DESC));
        return this;
    }

    @Override
    public List<Order> getAll(CriteriaBuilder criteriaBuilder, ConditionsBuilder rootBuilder, From<?, ?> root) {
        List<Order> orderList = new ArrayList<>();
        for (SingleOrder singleOrder : list) {
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

    protected class SingleOrder extends BaseJoinPair{
        private OrderType orderType;

        public SingleOrder(ConditionsBuilder joinBuilder, String attributeName, OrderType orderType) {
            super(joinBuilder, attributeName);
            this.orderType = orderType;
        }

        public OrderType getOrderType() {
            return orderType;
        }
    }
}
