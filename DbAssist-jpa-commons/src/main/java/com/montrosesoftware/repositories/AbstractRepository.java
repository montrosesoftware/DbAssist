package com.montrosesoftware.repositories;

import org.apache.commons.lang3.ClassUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Abstract class providing methods for reading entity data from DB
 * and necessary variables and interfaces used by these methods
 *
 * @param <T> the entity class
 */
public abstract class AbstractRepository<T> {

    private final Class<T> typeParameterClass;
    @PersistenceContext
    protected EntityManager entityManager;

    public AbstractRepository(Class<T> typeParameterClass) {
        this.typeParameterClass = typeParameterClass;
    }

    /**
     * The method prepares the SQL query to read the entity, applies fetch callbacks, conditions and sets specified
     * order on the generated query. Then it runs the query and returns the list of the entity objects.
     *
     * @param conditionsBuilder class containing conditions to apply on the query
     * @param fetchesBuilder    class containing fetch callbacks to apply on the query
     * @param orderBy            list specifying sorting of the result
     * @return list of found entities in the database
     */
    protected List<T> find(ConditionsBuilder conditionsBuilder, FetchesBuilder fetchesBuilder, OrderBy orderBy) {

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<T> criteriaQuery = criteriaBuilder.createQuery(typeParameterClass);
        Root<T> root = criteriaQuery.from(typeParameterClass);

        if (fetchesBuilder != null) {
            fetchesBuilder.applyFetches(root);
        }

        criteriaQuery.select(root);

        conditionsBuilder = applyConditions(conditionsBuilder, criteriaBuilder, criteriaQuery, root);

        if(orderBy != null){
            criteriaQuery.orderBy(orderBy.getAll(criteriaBuilder, conditionsBuilder, root));
        }

        TypedQuery<T> typedQuery = entityManager.createQuery(criteriaQuery);
        setParameters(conditionsBuilder, typedQuery);

        /**
         * Make sure that duplicate query results will be eliminated (when fetching collection relations of the root entity).
         */
        List list = new ArrayList(new LinkedHashSet(typedQuery.getResultList()));
        return list;
    }

    protected List<T> find(ConditionsBuilder conditionsBuilder) {
        return find(conditionsBuilder, null, null);
    }

    protected List<T> find(FetchesBuilder fetchesBuilder) {
        return find(null, fetchesBuilder, null);
    }

    protected List<T> find(ConditionsBuilder conditionsBuilder, FetchesBuilder fetchesBuilder) {
        return find(conditionsBuilder, fetchesBuilder, null);
    }

    protected List<T> find(ConditionsBuilder conditionsBuilder, OrderBy orderBy) {
        return find(conditionsBuilder, null, orderBy);
    }

    /**
     * The method prepares the SQL query to read a few specific attributes of the entity, applies fetch callbacks,
     * conditions and sets the specified order on the generated query. Then it runs the query and returns the list of
     * tuples (one tuple per found entity in the DB) containing the values read from the specified columns in the DB.
     *
     * @param selectionList     specifies which entity attributes to read or which aggregate methods to use
     * @param conditionsBuilder class containing conditions to apply on the query
     * @param fetchesBuilder    class containing fetch callbacks to apply on the query
     * @param orderBy            list specifying sorting of the result
     * @param groupBy           list of lambdas specifying grouping
     * @return list of tuples containing values corresponding to columns/aggregates specified in the selection list
     */
    protected List<Tuple> findAttributes(SelectionList selectionList,
                                         ConditionsBuilder conditionsBuilder,
                                         FetchesBuilder fetchesBuilder,
                                         OrderBy orderBy,
                                         GroupBy groupBy) {

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> criteriaQuery = criteriaBuilder.createTupleQuery();
        Root<T> root = criteriaQuery.from(typeParameterClass);

        if (fetchesBuilder != null) {
            fetchesBuilder.applyFetches(root);
        }

        criteriaQuery.multiselect(selectionList.getAll(criteriaBuilder, conditionsBuilder, root));

        conditionsBuilder = applyConditions(conditionsBuilder, criteriaBuilder, criteriaQuery, root);

        if(orderBy != null){
            criteriaQuery.orderBy(orderBy.getAll(criteriaBuilder, conditionsBuilder, root));
        }

        if (groupBy != null) {
            criteriaQuery.groupBy(groupBy.getAll(criteriaBuilder, conditionsBuilder, root));
        }

        TypedQuery<Tuple> typedQuery = entityManager.createQuery(criteriaQuery);

        setParameters(conditionsBuilder, typedQuery);

        return typedQuery.getResultList();
    }

    protected List<Tuple> findAttributes(SelectionList selectionList, ConditionsBuilder conditionsBuilder) {
        return findAttributes(selectionList, conditionsBuilder, null, null, null);
    }

    protected List<Tuple> findAttributes(SelectionList selectionList, ConditionsBuilder conditionsBuilder, OrderBy orderBy) {
        return findAttributes(selectionList, conditionsBuilder, null, orderBy, null);
    }

    protected List<Tuple> findAttributes(SelectionList selectionList, ConditionsBuilder conditionsBuilder, GroupBy groupBy) {
        return findAttributes(selectionList, conditionsBuilder, null, null, groupBy);
    }

    protected List<Tuple> findAttributes(SelectionList selectionList, ConditionsBuilder conditionsBuilder, OrderBy orderBy, GroupBy groupBy) {
        return findAttributes(selectionList, conditionsBuilder, null, orderBy, groupBy);
    }

    /**
     * The method prepares the SQL query to read a specific attribute of the entity, applies fetch callbacks, conditions and sets specified
     * order on the generated query. Then it runs the query and returns the list of the values read from the corresponding column in the DB.
     *
     * @param attributeName     the name of the entity attribute to read from the database
     * @param selectDistinct    specify whether duplicate query results will be eliminated
     * @param conditionsBuilder class containing conditions to apply on the query
     * @param fetchesBuilder    class containing fetch callbacks to apply on the query
     * @param orderBy            list specifying sorting of the result
     * @param selectCallback
     * @param <A>               the attribute class
     * @return list of the values read from the DB
     */
    private <A> List<A> findAttribute(String attributeName,
                                      boolean selectDistinct,
                                      ConditionsBuilder conditionsBuilder,
                                      FetchesBuilder fetchesBuilder,
                                      OrderBy orderBy,
                                      SelectFunction<CriteriaBuilder, Path<A>, Selection<A>> selectCallback) {

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<A> criteriaQuery = criteriaBuilder.createQuery(getType(attributeName));
        Root<T> root = criteriaQuery.from(typeParameterClass);

        if (fetchesBuilder != null) {
            fetchesBuilder.applyFetches(root);
        }

        Selection<? extends A> selection;

        if (selectCallback != null) {
            selection = selectCallback.apply(criteriaBuilder, root.get(attributeName));
        } else {
            selection = root.get(attributeName);
        }

        criteriaQuery.select(selection);

        if (selectDistinct) {
            criteriaQuery.distinct(true);
        }

        conditionsBuilder = applyConditions(conditionsBuilder, criteriaBuilder, criteriaQuery, root);

        if(orderBy != null){
            criteriaQuery.orderBy(orderBy.getAll(criteriaBuilder, conditionsBuilder, root));
        }

        TypedQuery<A> typedQuery = entityManager.createQuery(criteriaQuery);

        setParameters(conditionsBuilder, typedQuery);

        return typedQuery.getResultList();
    }

    protected <A> List<A> findAttribute(String attributeName,
                                        ConditionsBuilder conditionsBuilder,
                                        FetchesBuilder fetchesBuilder,
                                        OrderBy orderBy,
                                        SelectFunction<CriteriaBuilder, Path<A>, Selection<A>> selectCallback) {
        return findAttribute(attributeName, false, conditionsBuilder, fetchesBuilder, orderBy, selectCallback);
    }

    protected <A> List<A> findAttribute(String attributeName, ConditionsBuilder conditionsBuilder) {
        return findAttribute(attributeName, conditionsBuilder, null, null, null);
    }

    protected <A> List<A> findAttributeDistinct(String attributeName,
                                                ConditionsBuilder conditionsBuilder,
                                                FetchesBuilder fetchesBuilder,
                                                OrderBy orderBy,
                                                SelectFunction<CriteriaBuilder,
                                                        Path<A>, Selection<A>> selectCallback) {
        return findAttribute(attributeName, true, conditionsBuilder, fetchesBuilder, orderBy, selectCallback);
    }

    private <X> ConditionsBuilder applyConditions(ConditionsBuilder conditionsBuilder, CriteriaBuilder criteriaBuilder, CriteriaQuery<X> criteriaQuery, Root<T> root) {
        if (conditionsBuilder == null) {
            return null;
        }

        conditionsBuilder.applyConditions(criteriaQuery, criteriaBuilder, root);
        return conditionsBuilder;
    }

    private <X> void setParameters(ConditionsBuilder conditionsBuilder, TypedQuery<X> typedQuery) {
        if (conditionsBuilder != null) {
            conditionsBuilder.setParameters(typedQuery);
        }
    }

    private Class getType(String attributeName) {
        Metamodel metamodel = entityManager.getMetamodel();
        EntityType<T> entityType = metamodel.entity(typeParameterClass);
        Class attributeType = entityType.getAttribute(attributeName).getJavaType();
        return ClassUtils.primitiveToWrapper(attributeType);
    }

    protected <N extends Number> N min(ConditionsBuilder conditionsBuilder, String attributeName) {
        AggregateNum agg = new AggregateNum() {
            @Override
            public void prepareQuery(String attributeName) {
                cq.select(cb.min(root.get(attributeName)));
            }
        };
        return (N) agg.calculate(conditionsBuilder, attributeName);
    }

    protected <N extends Number> N max(ConditionsBuilder conditionsBuilder, String attributeName) {
        AggregateNum agg = new AggregateNum() {
            @Override
            public void prepareQuery(String attributeName) {
                cq.select(cb.max(root.get(attributeName)));
            }
        };
        return (N) agg.calculate(conditionsBuilder, attributeName);
    }

    protected <N extends Number> N sum(ConditionsBuilder conditionsBuilder, String attributeName) {
        AggregateNum agg = new AggregateNum() {
            @Override
            public void prepareQuery(String attributeName) {
                cq.select(cb.sum(root.get(attributeName)));
            }
        };
        return (N) agg.calculate(conditionsBuilder, attributeName);
    }

    protected Long sumAsLong(ConditionsBuilder conditionsBuilder, String attributeName) {
        AggregateNum agg = new AggregateNum() {
            @Override
            public void prepareQuery(String attributeName) {
                cq.select(cb.sumAsLong(root.get(attributeName)));
            }

            @Override
            protected Long prepareReturn(String attributeName, ConditionsBuilder conditionsBuilder) {
                return Long.class.cast(conditionsBuilder.setParameters(entityManager.createQuery(cq)).getSingleResult());
            }
        };
        return (Long) agg.calculate(conditionsBuilder, attributeName);
    }

    protected Double sumAsDouble(ConditionsBuilder conditionsBuilder, String attributeName) {
        AggregateNum agg = new AggregateNum() {
            @Override
            public void prepareQuery(String attributeName) {
                cq.select(cb.sumAsDouble(root.get(attributeName)));
            }

            @Override
            protected Double prepareReturn(String attributeName, ConditionsBuilder conditionsBuilder) {
                return (Double.class.cast(conditionsBuilder.setParameters(entityManager.createQuery(cq)).getSingleResult()));
            }
        };
        return (Double) agg.calculate(conditionsBuilder, attributeName);
    }

    protected Long count(ConditionsBuilder conditionsBuilder, boolean countDistinct) {
        AggregateNum agg = new AggregateNum(countDistinct) {
            @Override
            public void prepareQuery(String attributeName) {
                if (countDistinct)
                    cq.select(cb.countDistinct(root));
                else
                    cq.select(cb.count(root));
            }

            @Override
            protected Long prepareReturn(String attributeName, ConditionsBuilder conditions) {
                return (Long.class.cast(conditions.setParameters(entityManager.createQuery(cq)).getSingleResult()));
            }
        };
        return (Long) agg.calculate(conditionsBuilder, null);
    }

    protected Long count(ConditionsBuilder conditionsBuilder) {
        return count(conditionsBuilder, false);
    }

    protected Long countDistinct(ConditionsBuilder conditionsBuilder) {
        return count(conditionsBuilder, true);
    }

    protected Double avg(ConditionsBuilder conditionsBuilder, String attributeName) {
        AggregateNum agg = new AggregateNum() {
            @Override
            public void prepareQuery(String attributeName) {
                cq.select(cb.avg(root.get(attributeName)));
            }

            @Override
            protected Double prepareReturn(String attributeName, ConditionsBuilder conditionsBuilder) {
                return Double.class.cast(conditionsBuilder.setParameters(entityManager.createQuery(cq)).getSingleResult());
            }
        };
        return (Double) agg.calculate(conditionsBuilder, attributeName);
    }

    protected <N extends Comparable<N>> N least(ConditionsBuilder conditionsBuilder, String attributeName) {
        AggregateNonNum agg = new AggregateNonNum() {
            @Override
            public void prepareQuery(String attributeName) {
                cq.select(cb.least(root.<Comparable>get(attributeName)));
            }
        };
        return (N) agg.calculate(conditionsBuilder, attributeName);
    }

    protected <N extends Comparable<N>> N greatest(ConditionsBuilder conditionsBuilder, String attributeName) {
        AggregateNonNum agg = new AggregateNonNum() {
            @Override
            public void prepareQuery(String attributeName) {
                cq.select(cb.greatest(root.<Comparable>get(attributeName)));
            }
        };
        return (N) agg.calculate(conditionsBuilder, attributeName);
    }

    @FunctionalInterface
    interface SelectFunction<CB, P, S> {
        S apply(CB criteriaBuilder, P path);
    }

    private abstract class Aggregate {
        protected CriteriaBuilder cb;
        protected Root<T> root;

        public abstract void prepareQuery(String attributeName);
    }

    private abstract class AggregateNum extends Aggregate {

        protected CriteriaQuery<Number> cq;
        protected boolean countDistinct;

        public AggregateNum() {
        }

        public AggregateNum(boolean countDistinct) {
            this.countDistinct = countDistinct;
        }

        protected <N extends Number> N prepareReturn(String attributeName, ConditionsBuilder conditionsBuilder) {
            Class<? extends Number> attributeType = getType(attributeName);
            return (N) attributeType.cast(conditionsBuilder.setParameters(entityManager.createQuery(cq)).getSingleResult());
        }

        public Number calculate(ConditionsBuilder conditionsBuilder, String attributeName) {

            cb = entityManager.getCriteriaBuilder();
            cq = cb.createQuery(Number.class);
            root = cq.from(typeParameterClass);
            prepareQuery(attributeName);
            applyConditions(conditionsBuilder, cb, cq, root);
            return prepareReturn(attributeName, conditionsBuilder);
        }
    }

    private abstract class AggregateNonNum extends Aggregate {

        protected CriteriaQuery<Comparable> cq;

        public AggregateNonNum() {
        }

        protected <N extends Comparable<N>> N prepareReturn(String attributeName, ConditionsBuilder conditionsBuilder) {
            Class<? extends Comparable<N>> attributeType = getType(attributeName);
            return (N) attributeType.cast(conditionsBuilder.setParameters(entityManager.createQuery(cq)).getSingleResult());
        }

        public <N extends Comparable<N>> Comparable calculate(ConditionsBuilder conditionsBuilder, String attributeName) {

            cb = entityManager.getCriteriaBuilder();
            cq = cb.createQuery(Comparable.class);
            root = cq.from(typeParameterClass);
            prepareQuery(attributeName);
            applyConditions(conditionsBuilder, cb, cq, root);
            return prepareReturn(attributeName, conditionsBuilder);
        }
    }
}
