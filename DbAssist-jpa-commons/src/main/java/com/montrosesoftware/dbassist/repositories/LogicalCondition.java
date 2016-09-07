package com.montrosesoftware.dbassist.repositories;

import javax.persistence.criteria.CriteriaBuilder;

public abstract class LogicalCondition extends HierarchyCondition {

    protected HierarchyCondition left;
    protected HierarchyCondition right;

    public LogicalCondition(HierarchyCondition left, HierarchyCondition right) {
        this.left = left;
        this.right = right;
    }

    public static class AndCondition extends LogicalCondition {
        public AndCondition(HierarchyCondition left, HierarchyCondition right) {
            super(left, right);
        }

        @Override
        public Condition apply(ConditionsBuilder conditionsBuilder) {
            return conditionsBuilder.applyLogicalOperator(left.apply(conditionsBuilder), right.apply(conditionsBuilder), CriteriaBuilder::and);
        }
    }

    public static class OrCondition extends LogicalCondition {
        public OrCondition(HierarchyCondition left, HierarchyCondition right) {
            super(left, right);
        }

        @Override
        public Condition apply(ConditionsBuilder conditionsBuilder) {
            return conditionsBuilder.applyLogicalOperator(left.apply(conditionsBuilder), right.apply(conditionsBuilder), CriteriaBuilder::or);
        }
    }
}
