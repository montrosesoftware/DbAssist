package com.montrosesoftware.repositories;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;

public class HierarchyCondition {

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

    public static interface ApplicableCondition {
        Predicate apply(CriteriaBuilder cb, From<?, ?> root);
    }

    public static class Condition {
        private ApplicableCondition applicableCondition;
        private ConditionsBuilder conditionsBuilder;

        public Condition() {
        }

        public Condition(ConditionsBuilder conditionsBuilder, ApplicableCondition applicableCondition) {
            this.applicableCondition = applicableCondition;
            this.conditionsBuilder = conditionsBuilder;
        }

        public ApplicableCondition getApplicableCondition() {
            return applicableCondition;
        }

        public ConditionsBuilder getConditionsBuilder() {
            return conditionsBuilder;
        }
    }

    public enum LogicalOperator {
        AND,
        OR
    }
}
