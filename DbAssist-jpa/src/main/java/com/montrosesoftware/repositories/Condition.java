package com.montrosesoftware.repositories;

public class Condition {
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