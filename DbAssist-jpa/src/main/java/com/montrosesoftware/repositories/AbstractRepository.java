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
import java.util.function.Function;

public abstract class AbstractRepository<T> {

    @FunctionalInterface
    interface SelectFunction<CB, P, S> {
        S apply(CB criteriaBuilder, P path);
    }

    protected interface OrderBy<T> {
        List<Order> apply(CriteriaBuilder criteriaBuilder, Root<?> root);
    }

    protected interface GroupBy<T>  {
        List<Expression<?>> apply(Root<?> root);
    }

    protected interface SelectionList<T>  {
        List<Selection<?>> apply(CriteriaBuilder criteriaBuilder, Root<?> root);
    }

    private final Class<T> typeParameterClass;

    @PersistenceContext
    protected EntityManager entityManager;

    public AbstractRepository(Class<T> typeParameterClass) {
        this.typeParameterClass = typeParameterClass;
    }

    protected List<T> find(ConditionsBuilder conditionsBuilder, FetchesBuilder fetchesBuilder, OrderBy<T> orderBy) {

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<T> criteriaQuery = criteriaBuilder.createQuery(typeParameterClass);
        Root<T> root = criteriaQuery.from(typeParameterClass);

        if(fetchesBuilder != null) {
            fetchesBuilder.applyFetches(root);
        }

        criteriaQuery.select(root);

        conditionsBuilder = applyConditions(conditionsBuilder, criteriaBuilder, criteriaQuery, root);

        if (orderBy != null) {
            criteriaQuery.orderBy(orderBy.apply(criteriaBuilder, root));
        }

        TypedQuery<T> typedQuery = entityManager.createQuery(criteriaQuery);
        setParameters(conditionsBuilder, typedQuery);

        /**
         * Make sure that duplicate query results will be eliminated (when fetching collection relations of the root entity).
         */
        List list = new ArrayList(new LinkedHashSet(typedQuery.getResultList()));
        return list;
    }

    protected List<T> find(ConditionsBuilder conditionsBuilder){
        return find(conditionsBuilder, null, null);
    }

    protected List<Tuple> findAttributes(SelectionList<T> selectionList,
                                         ConditionsBuilder conditionsBuilder,
                                         FetchesBuilder fetchesBuilder,
                                         OrderBy<T> orderBy,
                                         GroupBy<T> groupBy) {

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> criteriaQuery = criteriaBuilder.createTupleQuery();
        Root<T> root = criteriaQuery.from(typeParameterClass);

        if(fetchesBuilder != null) {
            fetchesBuilder.applyFetches(root);
        }

        criteriaQuery.multiselect(selectionList.apply(criteriaBuilder, root));

        conditionsBuilder = applyConditions(conditionsBuilder, criteriaBuilder, criteriaQuery, root);

        if (orderBy != null) {
            criteriaQuery.orderBy(orderBy.apply(criteriaBuilder, root));
        }

        if (groupBy != null) {
            criteriaQuery.groupBy(groupBy.apply(root));
        }

        TypedQuery<Tuple> typedQuery = entityManager.createQuery(criteriaQuery);

        setParameters(conditionsBuilder, typedQuery);

        return typedQuery.getResultList();
    }

    protected List<Tuple> findAttributes(SelectionList<T> selectionList, ConditionsBuilder conditionsBuilder){
        return findAttributes(selectionList, conditionsBuilder, null, null, null);
    }

    protected <A> List<A> findAttribute(String attributeName,
                                        ConditionsBuilder conditionsBuilder,
                                        FetchesBuilder fetchesBuilder,
                                        OrderBy<T> orderBy,
                                        SelectFunction<CriteriaBuilder, Path<A>, Selection<A>> selectCallback) {
        return findAttribute(attributeName, false, conditionsBuilder, fetchesBuilder, orderBy, selectCallback);
    }

    protected <A> List<A> findAttributeDistinct(String attributeName,
                                                ConditionsBuilder conditionsBuilder,
                                                FetchesBuilder fetchesBuilder,
                                                OrderBy<T> orderBy,
                                                SelectFunction<CriteriaBuilder,
                                                        Path<A>, Selection<A>> selectCallback) {
        return findAttribute(attributeName, true, conditionsBuilder, fetchesBuilder, orderBy, selectCallback);
    }

    private <A> List<A> findAttribute(String attributeName,
                                      boolean selectDistinct,
                                      ConditionsBuilder conditionsBuilder,
                                      FetchesBuilder fetchesBuilder,
                                      OrderBy<T> orderBy,
                                      SelectFunction<CriteriaBuilder, Path<A>, Selection<A>> selectCallback) {

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<A> criteriaQuery = criteriaBuilder.createQuery(getType(attributeName));
        Root<T> root = criteriaQuery.from(typeParameterClass);

        if(fetchesBuilder != null) {
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

        if (orderBy != null) {
            criteriaQuery.orderBy(orderBy.apply(criteriaBuilder, root));
        }

        TypedQuery<A> typedQuery = entityManager.createQuery(criteriaQuery);

        setParameters(conditionsBuilder, typedQuery);

        return typedQuery.getResultList();
    }

    protected <A> List<A> findAttribute(String attributeName, ConditionsBuilder conditionsBuilder){
        return findAttribute(attributeName, conditionsBuilder, null, null, null);
    }

    private <X> ConditionsBuilder applyConditions(ConditionsBuilder conditionsBuilder, CriteriaBuilder criteriaBuilder, CriteriaQuery<X> criteriaQuery, Root<T> root) {
        if (conditionsBuilder == null) {
            return null;
        }

        conditionsBuilder.apply(criteriaQuery, criteriaBuilder, root);
        return conditionsBuilder;
    }

    private <X> void setParameters(ConditionsBuilder conditionsBuilder, TypedQuery<X> typedQuery) {
        if (conditionsBuilder != null) {
            conditionsBuilder.setParameters(typedQuery);
        }
    }

//    /**
//     * Multiple fetches might be chained together to force eager-loading of associations.
//     * See: http://stackoverflow.com/questions/8521338/how-to-fetch-all-data-in-one-query
//     */
//    private void applyFetchCallbacks(List<Function<FetchParent<?, ?>, FetchParent<?, ?>>> fetchCallbacks, Root<T> root) {
//        FetchParent<?, ?> rootFetchParent = root;
//        if (fetchCallbacks != null) {
//            for (Function<FetchParent<?, ?>, FetchParent<?, ?>> fetchCallback : fetchCallbacks) {
//                fetchCallback.apply(rootFetchParent);
//            }
//        }
//    }

    private Class getType(String attributeName){
        Metamodel metamodel = entityManager.getMetamodel();
        EntityType<T> entityType = metamodel.entity(typeParameterClass);
        Class attributeType = entityType.getAttribute(attributeName).getJavaType();
        return ClassUtils.primitiveToWrapper(attributeType);
    }

    private abstract class Aggregate {
        protected CriteriaBuilder cb;
        protected Root<T> root;

        public abstract void prepareQuery(String attributeName);
    }

    private abstract class AggregateNum extends Aggregate{

        public AggregateNum(){}

        public AggregateNum(boolean countDistinct){
            this.countDistinct = countDistinct;
        }

        protected CriteriaQuery<Number> cq;

        protected boolean countDistinct;

        protected  <N extends Number> N prepareReturn(String attributeName, ConditionsBuilder conditionsBuilder){
            Class<? extends Number> attributeType = getType(attributeName);
            return (N) attributeType.cast(conditionsBuilder.setParameters(entityManager.createQuery(cq)).getSingleResult());
        }

        public Number calculate(ConditionsBuilder conditionsBuilder, String attributeName){

            cb = entityManager.getCriteriaBuilder();
            cq = cb.createQuery(Number.class);
            root = cq.from(typeParameterClass);
            prepareQuery(attributeName);
            conditionsBuilder.apply(cq, cb, root);
            return prepareReturn(attributeName, conditionsBuilder);
        }
    }

    protected <N extends Number> N min(ConditionsBuilder conditionsBuilder, String attributeName){
        AggregateNum agg = new AggregateNum(){
            @Override
            public void prepareQuery(String attributeName){
                cq.select(cb.min(root.get(attributeName)));
            }
        };
        return (N) agg.calculate(conditionsBuilder, attributeName);
    }

    protected <N extends Number> N max(ConditionsBuilder conditionsBuilder, String attributeName){
        AggregateNum agg = new AggregateNum(){
          @Override
          public void prepareQuery(String attributeName){
              cq.select(cb.max(root.get(attributeName)));
          }
        };
        return (N) agg.calculate(conditionsBuilder, attributeName);
    }

    protected <N extends Number> N sum(ConditionsBuilder conditionsBuilder, String attributeName){
        AggregateNum agg = new AggregateNum(){
            @Override
            public void prepareQuery(String attributeName){
                cq.select(cb.sum(root.get(attributeName)));
            }
        };
        return (N) agg.calculate(conditionsBuilder, attributeName);
    }

    protected Long sumAsLong(ConditionsBuilder conditionsBuilder, String attributeName){
        AggregateNum agg = new AggregateNum(){
            @Override
            public void prepareQuery(String attributeName){
                cq.select(cb.sumAsLong(root.get(attributeName)));
            }

            @Override
            protected Long prepareReturn(String attributeName, ConditionsBuilder conditionsBuilder) {
                return Long.class.cast(conditionsBuilder.setParameters(entityManager.createQuery(cq)).getSingleResult());
            }
        };
        return (Long) agg.calculate(conditionsBuilder, attributeName);
    }

    protected Double sumAsDouble(ConditionsBuilder conditionsBuilder, String attributeName){
        AggregateNum agg = new AggregateNum(){
            @Override
            public void prepareQuery(String attributeName){
                cq.select(cb.sumAsDouble(root.get(attributeName)));
            }

            @Override
            protected Double prepareReturn(String attributeName, ConditionsBuilder conditionsBuilder) {
                return (Double.class.cast(conditionsBuilder.setParameters(entityManager.createQuery(cq)).getSingleResult()));
            }
        };
        return (Double) agg.calculate(conditionsBuilder, attributeName);
    }

    protected Long count(ConditionsBuilder conditionsBuilder, boolean countDistinct){
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

    protected Double avg(ConditionsBuilder conditionsBuilder, String attributeName){
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

    private abstract class AggregateNonNum extends Aggregate {

        public AggregateNonNum(){}

        protected CriteriaQuery<Comparable> cq;

        protected  <N extends Comparable<N>> N prepareReturn(String attributeName, ConditionsBuilder conditionsBuilder){
            Class<? extends Comparable<N>> attributeType = getType(attributeName);
            return (N) attributeType.cast(conditionsBuilder.setParameters(entityManager.createQuery(cq)).getSingleResult());
        }

        public <N extends Comparable<N>> Comparable calculate(ConditionsBuilder conditionsBuilder, String attributeName){

            cb = entityManager.getCriteriaBuilder();
            cq = cb.createQuery(Comparable.class);
            root = cq.from(typeParameterClass);
            prepareQuery(attributeName);
            conditionsBuilder.apply(cq, cb, root);
            return prepareReturn(attributeName, conditionsBuilder);
        }
    }

    protected <N extends Comparable<N>> N least(ConditionsBuilder conditionsBuilder, String attributeName){
        AggregateNonNum agg = new AggregateNonNum(){
            @Override
            public void prepareQuery(String attributeName){
                cq.select(cb.least(root.<Comparable>get(attributeName)));
            }
        };
        return (N)agg.calculate(conditionsBuilder, attributeName);
    }

    protected <N extends Comparable<N>> N greatest(ConditionsBuilder conditionsBuilder, String attributeName){
        AggregateNonNum agg = new AggregateNonNum(){
            @Override
            public void prepareQuery(String attributeName){
                cq.select(cb.greatest(root.<Comparable>get(attributeName)));
            }
        };
        return (N)agg.calculate(conditionsBuilder, attributeName);
    }
}
