package com.montrosesoftware.dbassist.repositories;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Selection;

public class SelectionList extends BaseJoinPairsList<BaseJoinPair<Selection<?>>, Selection<?>> {

    public SelectionList select(ConditionsBuilder joinBuilder, String attributeName) {
        list.add(new BaseJoinPair<Selection<?>>(joinBuilder, attributeName) {
            @Override
            public Selection<?> apply(CriteriaBuilder criteriaBuilder, ConditionsBuilder rootBuilder, From<?, ?> root) {
                return rootBuilder.getFrom(root, this.getJoinBuilder()).get(this.getAttributeName());
            }
        });
        return this;
    }

    public SelectionList count(ConditionsBuilder joinBuilder, String attributeName) {
        list.add(new BaseJoinPair<Selection<?>>(joinBuilder, attributeName) {
            @Override
            public Selection<?> apply(CriteriaBuilder criteriaBuilder, ConditionsBuilder rootBuilder, From root) {
                return criteriaBuilder.count(rootBuilder.getFrom(root, this.getJoinBuilder()).get(this.getAttributeName()));
            }
        });
        return this;
    }

    public SelectionList avg(ConditionsBuilder joinBuilder, String attributeName) {
        list.add(new BaseJoinPair<Selection<?>>(joinBuilder, attributeName) {
            @Override
            public Selection<?> apply(CriteriaBuilder criteriaBuilder, ConditionsBuilder rootBuilder, From root) {
                return criteriaBuilder.avg(rootBuilder.getFrom(root, this.getJoinBuilder()).get(this.getAttributeName()));
            }
        });
        return this;
    }

    public SelectionList sum(ConditionsBuilder joinBuilder, String attributeName) {
        list.add(new BaseJoinPair<Selection<?>>(joinBuilder, attributeName) {
            @Override
            public Selection<?> apply(CriteriaBuilder criteriaBuilder, ConditionsBuilder rootBuilder, From root) {
                return criteriaBuilder.sum(rootBuilder.getFrom(root, this.getJoinBuilder()).get(this.getAttributeName()));
            }
        });
        return this;
    }

    public SelectionList sumAsLong(ConditionsBuilder joinBuilder, String attributeName) {
        list.add(new BaseJoinPair<Selection<?>>(joinBuilder, attributeName) {
            @Override
            public Selection<?> apply(CriteriaBuilder criteriaBuilder, ConditionsBuilder rootBuilder, From root) {
                return criteriaBuilder.sumAsLong(rootBuilder.getFrom(root, this.getJoinBuilder()).get(this.getAttributeName()));
            }
        });
        return this;
    }

    public SelectionList sumAsDouble(ConditionsBuilder joinBuilder, String attributeName) {
        list.add(new BaseJoinPair<Selection<?>>(joinBuilder, attributeName) {
            @Override
            public Selection<?> apply(CriteriaBuilder criteriaBuilder, ConditionsBuilder rootBuilder, From root) {
                return criteriaBuilder.sumAsDouble(rootBuilder.getFrom(root, this.getJoinBuilder()).get(this.getAttributeName()));
            }
        });
        return this;
    }

    public SelectionList min(ConditionsBuilder joinBuilder, String attributeName) {
        list.add(new BaseJoinPair<Selection<?>>(joinBuilder, attributeName) {
            @Override
            public Selection<?> apply(CriteriaBuilder criteriaBuilder, ConditionsBuilder rootBuilder, From root) {
                return criteriaBuilder.min(rootBuilder.getFrom(root, this.getJoinBuilder()).get(this.getAttributeName()));
            }
        });
        return this;
    }

    public SelectionList max(ConditionsBuilder joinBuilder, String attributeName) {
        list.add(new BaseJoinPair<Selection<?>>(joinBuilder, attributeName) {
            @Override
            public Selection<?> apply(CriteriaBuilder criteriaBuilder, ConditionsBuilder rootBuilder, From root) {
                return criteriaBuilder.max(rootBuilder.getFrom(root, this.getJoinBuilder()).get(this.getAttributeName()));
            }
        });
        return this;
    }
}
