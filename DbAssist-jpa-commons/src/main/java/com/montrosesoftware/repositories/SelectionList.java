package com.montrosesoftware.repositories;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Selection;
import java.util.ArrayList;
import java.util.List;

public class SelectionList extends BaseSinglesList<SelectionList.SingleSelect, Selection<?>> {

    public SelectionList select(ConditionsBuilder joinBuilder, String attributeName) {
        list.add(new SingleSelect(joinBuilder, attributeName));
        return this;
    }

    public SelectionList count(ConditionsBuilder joinBuilder, String attributeName) {
        list.add(new SingleSelect(joinBuilder, attributeName) {
            @Override
            public void addToSelections(List<Selection<?>> selections, CriteriaBuilder criteriaBuilder, ConditionsBuilder rootBuilder, From<?, ?> root) {
                selections.add(criteriaBuilder.count(rootBuilder.getFrom(root, this.getJoinBuilder()).get(this.getAttributeName())));
            }
        });
        return this;
    }

    public SelectionList avg(ConditionsBuilder joinBuilder, String attributeName) {
        list.add(new SingleSelect(joinBuilder, attributeName) {
            @Override
            public void addToSelections(List<Selection<?>> selections, CriteriaBuilder criteriaBuilder, ConditionsBuilder rootBuilder, From<?, ?> root) {
                selections.add(criteriaBuilder.avg(rootBuilder.getFrom(root, this.getJoinBuilder()).get(this.getAttributeName())));
            }
        });
        return this;
    }

    public SelectionList sum(ConditionsBuilder joinBuilder, String attributeName) {
        list.add(new SingleSelect(joinBuilder, attributeName) {
            @Override
            public void addToSelections(List<Selection<?>> selections, CriteriaBuilder criteriaBuilder, ConditionsBuilder rootBuilder, From<?, ?> root) {
                selections.add(criteriaBuilder.sum(rootBuilder.getFrom(root, this.getJoinBuilder()).get(this.getAttributeName())));
            }
        });
        return this;
    }

    public SelectionList sumAsLong(ConditionsBuilder joinBuilder, String attributeName) {
        list.add(new SingleSelect(joinBuilder, attributeName) {
            @Override
            public void addToSelections(List<Selection<?>> selections, CriteriaBuilder criteriaBuilder, ConditionsBuilder rootBuilder, From<?, ?> root) {
                selections.add(criteriaBuilder.sumAsLong(rootBuilder.getFrom(root, this.getJoinBuilder()).get(this.getAttributeName())));
            }
        });
        return this;
    }

    public SelectionList sumAsDouble(ConditionsBuilder joinBuilder, String attributeName) {
        list.add(new SingleSelect(joinBuilder, attributeName) {
            @Override
            public void addToSelections(List<Selection<?>> selections, CriteriaBuilder criteriaBuilder, ConditionsBuilder rootBuilder, From<?, ?> root) {
                selections.add(criteriaBuilder.sumAsDouble(rootBuilder.getFrom(root, this.getJoinBuilder()).get(this.getAttributeName())));
            }
        });
        return this;
    }

    public SelectionList min(ConditionsBuilder joinBuilder, String attributeName) {
        list.add(new SingleSelect(joinBuilder, attributeName) {
            @Override
            public void addToSelections(List<Selection<?>> selections, CriteriaBuilder criteriaBuilder, ConditionsBuilder rootBuilder, From<?, ?> root) {
                selections.add(criteriaBuilder.min(rootBuilder.getFrom(root, this.getJoinBuilder()).get(this.getAttributeName())));
            }
        });
        return this;
    }

    public SelectionList max(ConditionsBuilder joinBuilder, String attributeName) {
        list.add(new SingleSelect(joinBuilder, attributeName) {
            @Override
            public void addToSelections(List<Selection<?>> selections, CriteriaBuilder criteriaBuilder, ConditionsBuilder rootBuilder, From<?, ?> root) {
                selections.add(criteriaBuilder.max(rootBuilder.getFrom(root, this.getJoinBuilder()).get(this.getAttributeName())));
            }
        });
        return this;
    }

    @Override
    public List<Selection<?>> getAll(CriteriaBuilder criteriaBuilder, ConditionsBuilder rootBuilder, From<?, ?> root) {
        List<Selection<?>> selections = new ArrayList<>();
        for (SingleSelect singleSelect : list)
            singleSelect.addToSelections(selections, criteriaBuilder, rootBuilder, root);
        return selections;
    }

    protected class SingleSelect extends BaseJoinPair {
        public SingleSelect(ConditionsBuilder joinBuilder, String attributeName) {
            super(joinBuilder, attributeName);
        }

        public void addToSelections(List<Selection<?>> selections, CriteriaBuilder criteriaBuilder, ConditionsBuilder rootBuilder, From<?, ?> root) {
            selections.add(rootBuilder.getFrom(root, this.getJoinBuilder()).get(this.getAttributeName()));
        }
    }
}
