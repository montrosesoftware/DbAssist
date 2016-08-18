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
import java.util.Date;
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

    protected List<T> find(Conditions conditions, List<Function<FetchParent<?, ?>, FetchParent<?, ?>>> fetchCallbacks, OrderBy<T> orderBy) {
        if(conditions.isConditionsAlreadyUsed())
            return null;

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<T> criteriaQuery = criteriaBuilder.createQuery(typeParameterClass);
        Root<T> root = criteriaQuery.from(typeParameterClass);

        applyFetchCallbacks(fetchCallbacks, root);

        criteriaQuery.select(root);

        conditions = applyConditions(conditions, criteriaBuilder, criteriaQuery, root);
        conditions.setConditionsAlreadyUsed();

        if (orderBy != null) {
            criteriaQuery.orderBy(orderBy.apply(criteriaBuilder, root));
        }

        TypedQuery<T> typedQuery = entityManager.createQuery(criteriaQuery);
        setParameters(conditions, typedQuery);

        /**
         * Make sure that duplicate query results will be eliminated (when fetching collection relations of the root entity).
         */
        return new ArrayList(new LinkedHashSet(typedQuery.getResultList()));
    }

    protected List<T> find(Conditions conditions){
        return find(conditions, null, null);
    }

    protected List<Tuple> findAttributes(SelectionList<T> selectionList,
                                         Conditions conditions,
                                         List<Function<FetchParent<?, ?>, FetchParent<?, ?>>> fetchCallbacks,
                                         OrderBy<T> orderBy,
                                         GroupBy<T> groupBy) {
        if(conditions.isConditionsAlreadyUsed())
            return null;

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> criteriaQuery = criteriaBuilder.createTupleQuery();
        Root<T> root = criteriaQuery.from(typeParameterClass);

        applyFetchCallbacks(fetchCallbacks, root);

        criteriaQuery.multiselect(selectionList.apply(criteriaBuilder, root));

        conditions = applyConditions(conditions, criteriaBuilder, criteriaQuery, root);
        conditions.setConditionsAlreadyUsed();

        if (orderBy != null) {
            criteriaQuery.orderBy(orderBy.apply(criteriaBuilder, root));
        }

        if (groupBy != null) {
            criteriaQuery.groupBy(groupBy.apply(root));
        }

        TypedQuery<Tuple> typedQuery = entityManager.createQuery(criteriaQuery);

        setParameters(conditions, typedQuery);

        return typedQuery.getResultList();
    }

    protected List<Tuple> findAttributes(SelectionList<T> selectionList, Conditions conditions){
        return findAttributes(selectionList, conditions, null, null, null);
    }

    protected <A> List<A> findAttribute(String attributeName,
                                        Conditions conditions,
                                        List<Function<FetchParent<?, ?>, FetchParent<?, ?>>> fetchCallbacks,
                                        OrderBy<T> orderBy,
                                        SelectFunction<CriteriaBuilder, Path<A>, Selection<A>> selectCallback) {
        return findAttribute(attributeName, false, conditions, fetchCallbacks, orderBy, selectCallback);
    }

    protected <A> List<A> findAttributeDistinct(String attributeName,
                                                Conditions conditions,
                                                List<Function<FetchParent<?, ?>, FetchParent<?, ?>>> fetchCallbacks,
                                                OrderBy<T> orderBy,
                                                SelectFunction<CriteriaBuilder,
                                                        Path<A>, Selection<A>> selectCallback) {
        return findAttribute(attributeName, true, conditions, fetchCallbacks, orderBy, selectCallback);
    }

    private <A> List<A> findAttribute(String attributeName,
                                      boolean selectDistinct,
                                      Conditions conditions,
                                      List<Function<FetchParent<?, ?>, FetchParent<?, ?>>> fetchCallbacks,
                                      OrderBy<T> orderBy,
                                      SelectFunction<CriteriaBuilder, Path<A>, Selection<A>> selectCallback) {
        if(conditions.isConditionsAlreadyUsed())
            return null;

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<A> criteriaQuery = criteriaBuilder.createQuery(getType(attributeName));
        Root<T> root = criteriaQuery.from(typeParameterClass);

        applyFetchCallbacks(fetchCallbacks, root);

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

        conditions = applyConditions(conditions, criteriaBuilder, criteriaQuery, root);
        conditions.setConditionsAlreadyUsed();

        if (orderBy != null) {
            criteriaQuery.orderBy(orderBy.apply(criteriaBuilder, root));
        }

        TypedQuery<A> typedQuery = entityManager.createQuery(criteriaQuery);

        setParameters(conditions, typedQuery);

        return typedQuery.getResultList();
    }

    protected <A> List<A> findAttribute(String attributeName, Conditions conditions){
        return findAttribute(attributeName, conditions, null, null, null);
    }

    private <X> Conditions applyConditions(Conditions conditions, CriteriaBuilder criteriaBuilder, CriteriaQuery<X> criteriaQuery, Root<T> root) {
        if (conditions == null) {
            return null;
        }

        conditions.apply(criteriaQuery, criteriaBuilder, root);
        return conditions;
    }

    private <X> void setParameters(Conditions conditions, TypedQuery<X> typedQuery) {
        if (conditions != null) {
            conditions.setParameters(typedQuery);
        }
    }

    /**
     * Multiple fetches might be chained together to force eager-loading of associations.
     * See: http://stackoverflow.com/questions/8521338/how-to-fetch-all-data-in-one-query
     */
    private void applyFetchCallbacks(List<Function<FetchParent<?, ?>, FetchParent<?, ?>>> fetchCallbacks, Root<T> root) {
        FetchParent<?, ?> rootFetchParent = root;
        if (fetchCallbacks != null) {
            for (Function<FetchParent<?, ?>, FetchParent<?, ?>> fetchCallback : fetchCallbacks) {
                fetchCallback.apply(rootFetchParent);
            }
        }
    }

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

        protected  <N extends Number> N prepareReturn(String attributeName, Conditions conditions){
            Class<? extends Number> attributeType = getType(attributeName);
            return (N) attributeType.cast(conditions.setParameters(entityManager.createQuery(cq)).getSingleResult());
        }

        public Number calculate(Conditions conditions, String attributeName){
            if (conditions.isConditionsAlreadyUsed())
                return null;

            cb = entityManager.getCriteriaBuilder();
            cq = cb.createQuery(Number.class);
            root = cq.from(typeParameterClass);
            prepareQuery(attributeName);
            conditions.apply(cq, cb, root);
            conditions.setConditionsAlreadyUsed();
            return prepareReturn(attributeName, conditions);
        }
    }

    protected <N extends Number> N min(Conditions conditions, String attributeName){
        AggregateNum agg = new AggregateNum(){
            @Override
            public void prepareQuery(String attributeName){
                cq.select(cb.min(root.get(attributeName)));
            }
        };
        return (N) agg.calculate(conditions, attributeName);
    }

    protected <N extends Number> N max(Conditions conditions, String attributeName){
        AggregateNum agg = new AggregateNum(){
          @Override
          public void prepareQuery(String attributeName){
              cq.select(cb.max(root.get(attributeName)));
          }
        };
        return (N) agg.calculate(conditions, attributeName);
    }

    protected <N extends Number> N sum(Conditions conditions, String attributeName){
        AggregateNum agg = new AggregateNum(){
            @Override
            public void prepareQuery(String attributeName){
                cq.select(cb.sum(root.get(attributeName)));
            }
        };
        return (N) agg.calculate(conditions, attributeName);
    }

    protected Long sumAsLong(Conditions conditions, String attributeName){
        AggregateNum agg = new AggregateNum(){
            @Override
            public void prepareQuery(String attributeName){
                cq.select(cb.sumAsLong(root.get(attributeName)));
            }

            @Override
            protected Long prepareReturn(String attributeName, Conditions conditions) {
                return Long.class.cast(conditions.setParameters(entityManager.createQuery(cq)).getSingleResult());
            }
        };
        return (Long) agg.calculate(conditions, attributeName);
    }

    protected Double sumAsDouble(Conditions conditions, String attributeName){
        AggregateNum agg = new AggregateNum(){
            @Override
            public void prepareQuery(String attributeName){
                cq.select(cb.sumAsDouble(root.get(attributeName)));
            }

            @Override
            protected Double prepareReturn(String attributeName, Conditions conditions) {
                return (Double.class.cast(conditions.setParameters(entityManager.createQuery(cq)).getSingleResult()));
            }
        };
        return (Double) agg.calculate(conditions, attributeName);
    }

    protected Long count(Conditions conditions, boolean countDistinct){
        AggregateNum agg = new AggregateNum(countDistinct) {
            @Override
            public void prepareQuery(String attributeName) {
                if (countDistinct)
                    cq.select(cb.countDistinct(root));
                else
                    cq.select(cb.count(root));
            }

            @Override
            protected Long prepareReturn(String attributeName, Conditions conditions) {
                return (Long.class.cast(conditions.setParameters(entityManager.createQuery(cq)).getSingleResult()));
            }
        };
        return (Long) agg.calculate(conditions, null);
    }

    protected Long count(Conditions conditions) {
        return count(conditions, false);
    }

    protected Long countDistinct(Conditions conditions) {
        return count(conditions, true);
    }

    protected Double avg(Conditions conditions, String attributeName){
        AggregateNum agg = new AggregateNum() {
            @Override
            public void prepareQuery(String attributeName) {
                cq.select(cb.avg(root.get(attributeName)));
            }

            @Override
            protected Double prepareReturn(String attributeName, Conditions conditions) {
                return Double.class.cast(conditions.setParameters(entityManager.createQuery(cq)).getSingleResult());
            }
        };
        return (Double) agg.calculate(conditions, attributeName);
    }

    private abstract class AggregateNonNum extends Aggregate {

        public AggregateNonNum(){}

        protected CriteriaQuery<Comparable> cq;

        protected  <N extends Comparable<N>> N prepareReturn(String attributeName, Conditions conditions){
            Class<? extends Comparable<N>> attributeType = getType(attributeName);
            return (N) attributeType.cast(conditions.setParameters(entityManager.createQuery(cq)).getSingleResult());
        }

        public <N extends Comparable<N>> Comparable calculate(Conditions conditions, String attributeName){
            if (conditions.isConditionsAlreadyUsed())
                return null;

            cb = entityManager.getCriteriaBuilder();
            cq = cb.createQuery(Comparable.class);
            root = cq.from(typeParameterClass);
            prepareQuery(attributeName);
            conditions.apply(cq, cb, root);
            conditions.setConditionsAlreadyUsed();
            return prepareReturn(attributeName, conditions);
        }
    }

    protected <N extends Comparable<N>> N least(Conditions conditions, String attributeName){
        AggregateNonNum agg = new AggregateNonNum(){
            @Override
            public void prepareQuery(String attributeName){
                cq.select(cb.least(root.<Comparable>get(attributeName)));
            }
        };
        return (N)agg.calculate(conditions, attributeName);
    }

    protected <N extends Comparable<N>> N greatest(Conditions conditions, String attributeName){
        AggregateNonNum agg = new AggregateNonNum(){
            @Override
            public void prepareQuery(String attributeName){
                cq.select(cb.greatest(root.<Comparable>get(attributeName)));
            }
        };
        return (N)agg.calculate(conditions, attributeName);
    }
}
