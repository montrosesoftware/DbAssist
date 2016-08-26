package com.montrosesoftware.repositories;

import javax.persistence.criteria.JoinType;
import java.util.HashMap;

public abstract class BaseBuilder<T extends BaseBuilder> {

    protected HashMap<String, T> builders = new HashMap<>();

    protected T parent = null;

    protected String joinAttribute;

    protected JoinType joinType;

    public BaseBuilder(){}

    public BaseBuilder(String joinAttribute, JoinType joinType, T parent) {
        this.joinAttribute = joinAttribute;
        this.joinType = joinType;
        this.parent = parent;
    }

    public HashMap<String, T> getBuilders() {
        return builders;
    }

    public abstract T getInstance(String joinAttribute, JoinType joinType, T parent);

    protected T getBuilder(String joinAttribute, JoinType joinType) {
        T builder = builders.get(joinAttribute);

        if (builder == null) {
            builder = getInstance(joinAttribute, joinType, (T) this);
            builders.put(joinAttribute, builder);
        }

        return builder;
    }

    public T getParent() {
        return parent;
    }
}
