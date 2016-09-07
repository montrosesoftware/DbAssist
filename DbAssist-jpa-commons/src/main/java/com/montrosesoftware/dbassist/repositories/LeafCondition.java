package com.montrosesoftware.dbassist.repositories;

public class LeafCondition extends HierarchyCondition {

    private Condition condition;

    public LeafCondition(Condition condition) {
        this.condition = condition;
    }

    public LeafCondition(ConditionsBuilder conditionsBuilder, ApplicableCondition applicableCondition) {
        this.condition = new Condition(conditionsBuilder, applicableCondition);
    }

    @Override
    public Condition apply(ConditionsBuilder conditionsBuilder) {
        return condition;
    }

}
