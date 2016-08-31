package com.montrosesoftware.repositories;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;

/**
 * The class implements composite design patterns and is intended to
 * store the hierarchy of logical and/or operations on the single conditions (leafs)
 */
public abstract class HierarchyCondition {

    public abstract Condition apply(ConditionsBuilder conditionsBuilder);

    public interface ApplicableCondition {
        Predicate apply(CriteriaBuilder cb, From<?, ?> root);
    }

    /**
     * The class is a container of the predicate to apply and the ConditionsBuilder we want to apply it on
     */
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
