package com.montrosesoftware.repositories;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Selection;
import java.util.ArrayList;
import java.util.List;

public class SelectionList {

    private List<SingleSelect> selects = new ArrayList<>();

    public SelectionList() {
    }

    public SelectionList select(ConditionsBuilder joinBuilder, String attributeName) {
        selects.add(new SingleSelect(joinBuilder, attributeName));
        return this;
    }

    public SelectionList count(ConditionsBuilder joinBuilder, String attributeName) {
        selects.add(new SingleSelect(joinBuilder, attributeName) {
            @Override
            public void addToSelections(List<Selection<?>> selections, CriteriaBuilder criteriaBuilder, ConditionsBuilder rootBuilder, From<?, ?> root) {
                selections.add(criteriaBuilder.count(rootBuilder.getFrom(root, this.getJoinBuilder()).get(this.getAttributeName())));
            }
        });
        return this;
    }

    public SelectionList avg(ConditionsBuilder joinBuilder, String attributeName) {
        selects.add(new SingleSelect(joinBuilder, attributeName) {
            @Override
            public void addToSelections(List<Selection<?>> selections, CriteriaBuilder criteriaBuilder, ConditionsBuilder rootBuilder, From<?, ?> root) {
                selections.add(criteriaBuilder.avg(rootBuilder.getFrom(root, this.getJoinBuilder()).get(this.getAttributeName())));
            }
        });
        return this;
    }

    public SelectionList sum(ConditionsBuilder joinBuilder, String attributeName) {
        selects.add(new SingleSelect(joinBuilder, attributeName) {
            @Override
            public void addToSelections(List<Selection<?>> selections, CriteriaBuilder criteriaBuilder, ConditionsBuilder rootBuilder, From<?, ?> root) {
                selections.add(criteriaBuilder.sum(rootBuilder.getFrom(root, this.getJoinBuilder()).get(this.getAttributeName())));
            }
        });
        return this;
    }

    //TODO add more methods for other aggregates

    List<Selection<?>> getSelectionList(CriteriaBuilder criteriaBuilder, ConditionsBuilder rootBuilder, From<?, ?> root) {
        List<Selection<?>> selections = new ArrayList<>();
        for (SingleSelect singleSelect : selects)
            singleSelect.addToSelections(selections, criteriaBuilder, rootBuilder, root);
        return selections;
    }

    private class SingleSelect {
        protected ConditionsBuilder joinBuilder;
        protected String attributeName;

        public SingleSelect(ConditionsBuilder joinBuilder, String attributeName) {
            this.joinBuilder = joinBuilder;
            this.attributeName = attributeName;
        }

        public void addToSelections(List<Selection<?>> selections, CriteriaBuilder criteriaBuilder, ConditionsBuilder rootBuilder, From<?, ?> root) {
            selections.add(rootBuilder.getFrom(root, this.getJoinBuilder()).get(this.getAttributeName()));
        }

        public ConditionsBuilder getJoinBuilder() {
            return joinBuilder;
        }

        public String getAttributeName() {
            return attributeName;
        }
    }
}
