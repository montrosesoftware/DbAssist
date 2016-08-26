package com.montrosesoftware.repositories;

import javax.persistence.criteria.CriteriaBuilder;

public class HierarchyCondition {

    public enum LogicalOperator {
        AND,
        OR
    }

    public HierarchyCondition() {
    }

    public HierarchyCondition(Condition condition) {
        this.condition = condition;
    }

    public HierarchyCondition(ConditionsBuilder conditionsBuilder, ApplicableCondition applicableCondition) {
        this.condition = new Condition(conditionsBuilder, applicableCondition);
    }

    public HierarchyCondition(HierarchyCondition left, HierarchyCondition right, LogicalOperator logicalOperator) {
        this.left = left;
        this.right = right;
        this.logicalOperator = logicalOperator;
    }

    private Condition condition;
    private HierarchyCondition left;
    private HierarchyCondition right;

    private LogicalOperator logicalOperator;

    public Condition apply(ConditionsBuilder conditionsBuilder) {
        if (left != null && right != null) {
            if (logicalOperator == LogicalOperator.AND)
                return conditionsBuilder.applyLogicalOperator(left.apply(conditionsBuilder), right.apply(conditionsBuilder), CriteriaBuilder::and);

            else
                return conditionsBuilder.applyLogicalOperator(left.apply(conditionsBuilder), right.apply(conditionsBuilder), CriteriaBuilder::or);
        }
        return condition;
    }
}
