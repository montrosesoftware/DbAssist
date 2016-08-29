package com.montrosesoftware.repositories;

import javax.persistence.criteria.CriteriaBuilder;

public class ParentCondition extends HierarchyCondition {

    private HierarchyCondition left;
    private HierarchyCondition right;

    private LogicalOperator logicalOperator;

    public ParentCondition(HierarchyCondition left, HierarchyCondition right, LogicalOperator logicalOperator) {
        this.left = left;
        this.right = right;
        this.logicalOperator = logicalOperator;
    }

    @Override
    public Condition apply(ConditionsBuilder conditionsBuilder) {
        if (logicalOperator == LogicalOperator.AND)
            return conditionsBuilder.applyLogicalOperator(left.apply(conditionsBuilder), right.apply(conditionsBuilder), CriteriaBuilder::and);
        else
            return conditionsBuilder.applyLogicalOperator(left.apply(conditionsBuilder), right.apply(conditionsBuilder), CriteriaBuilder::or);
    }

    public enum LogicalOperator {
        AND,
        OR
    }
}
