package com.montrosesoftware.repositories;

public class BaseJoinPair {
    protected ConditionsBuilder joinBuilder;
    protected String attributeName;

    public BaseJoinPair(ConditionsBuilder joinBuilder, String attributeName) {
        this.joinBuilder = joinBuilder;
        this.attributeName = attributeName;
    }

    public ConditionsBuilder getJoinBuilder() {
        return joinBuilder;
    }

    public String getAttributeName() {
        return attributeName;
    }
}
