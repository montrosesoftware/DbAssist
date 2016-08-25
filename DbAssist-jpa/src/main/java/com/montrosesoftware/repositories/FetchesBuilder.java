package com.montrosesoftware.repositories;

import javax.persistence.criteria.FetchParent;
import javax.persistence.criteria.JoinType;
import java.util.HashMap;

public class FetchesBuilder {

    private HashMap<String, FetchesBuilder> fetchesBuilders = new HashMap<>();

    private String joinAttribute;

    private JoinType joinType;

    public FetchesBuilder(){}

    public FetchesBuilder(String joinAttribute, JoinType joinType) {
        this.joinAttribute = joinAttribute;
        this.joinType = joinType;
    }

    public FetchesBuilder fetch(String joinAttribute, JoinType joinType) {
        FetchesBuilder fetchesBuilder = fetchesBuilders.get(joinAttribute);

        if (fetchesBuilder == null) {
            fetchesBuilder = new FetchesBuilder(joinAttribute, joinType);
            fetchesBuilders.put(joinAttribute, fetchesBuilder);
        }

        return fetchesBuilder;
    }

    public void applyFetches(FetchParent<?, ?> root){
        //apply itself
        FetchParent<?, ?> rootFetchParent = root;
        if (!(joinAttribute == null))
            rootFetchParent = root.fetch(joinAttribute, joinType);

        for(FetchesBuilder fb : fetchesBuilders.values()){
            fb.applyFetches(rootFetchParent);
        }
    }
}
