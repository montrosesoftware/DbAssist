package com.montrosesoftware.repositories;

import javax.persistence.criteria.FetchParent;
import javax.persistence.criteria.JoinType;

public class FetchesBuilder extends BaseBuilder<FetchesBuilder> {

    public FetchesBuilder(){}

    public FetchesBuilder(String joinAttribute, JoinType joinType, FetchesBuilder parent) {
        super(joinAttribute, joinType, parent);
    }

    @Override
    public FetchesBuilder getInstance(String joinAttribute, JoinType joinType, FetchesBuilder parent) {
        return new FetchesBuilder(joinAttribute, joinType, parent);
    }

    /**
     * Method creates or retrieves existing FetchesBuilder corresponding to the fetch specified by joinAttribute and joinType
     */
    public FetchesBuilder fetch(String joinAttribute, JoinType joinType) {
        return getBuilder(joinAttribute, joinType);
    }

    public void applyFetches(FetchParent<?, ?> root){
        //apply itself
        FetchParent<?, ?> rootFetchParent = root;
        if (!(joinAttribute == null))
            rootFetchParent = root.fetch(joinAttribute, joinType);

        for(FetchesBuilder fb : builders.values()){
            fb.applyFetches(rootFetchParent);
        }
    }
}
