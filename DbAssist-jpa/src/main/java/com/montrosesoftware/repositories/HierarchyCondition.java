package com.montrosesoftware.repositories;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;

public abstract class HierarchyCondition {

    public abstract Condition apply(ConditionsBuilder conditionsBuilder);

    public interface ApplicableCondition {
        Predicate apply(CriteriaBuilder cb, From<?, ?> root);
    }

    public static class Condition {
        private ApplicableCondition applicableCondition;
        private ConditionsBuilder conditionsBuilder;

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
}
