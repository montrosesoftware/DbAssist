package com.montrosesoftware.repositories;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
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

    protected List<T> find(Conditions conditions, List<Function<FetchParent<?, ?>, FetchParent<?, ?>>> fetchCallbacks, OrderBy<T> orderBy) {
        //TODO apply to other find*, count etc.
        if(conditions.isConditionsAlreadyUsed())
            return null;
        else
            conditions.setConditionsAlreadyUsed();

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<T> criteriaQuery = criteriaBuilder.createQuery(typeParameterClass);
        Root<T> root = criteriaQuery.from(typeParameterClass);

        applyFetchCallbacks(fetchCallbacks, root);

        criteriaQuery.select(root);

        conditions = applyConditions(conditions, criteriaBuilder, criteriaQuery, root);

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
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> criteriaQuery = criteriaBuilder.createTupleQuery();
        Root<T> root = criteriaQuery.from(typeParameterClass);

        applyFetchCallbacks(fetchCallbacks, root);

        criteriaQuery.multiselect(selectionList.apply(criteriaBuilder, root));

        conditions = applyConditions(conditions, criteriaBuilder, criteriaQuery, root);

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

    protected <A> List<A> findAttribute(Class<A> attributeClass,
                                        String attributeName,
                                        Conditions conditions,
                                        List<Function<FetchParent<?, ?>, FetchParent<?, ?>>> fetchCallbacks,
                                        OrderBy<T> orderBy,
                                        SelectFunction<CriteriaBuilder, Path<A>, Selection<A>> selectCallback) {
        return findAttribute(attributeClass, attributeName, false, conditions, fetchCallbacks, orderBy, selectCallback);
    }

    protected <A> List<A> findAttributeDistinct(Class<A> attributeClass,
                                                String attributeName,
                                                Conditions conditions,
                                                List<Function<FetchParent<?, ?>, FetchParent<?, ?>>> fetchCallbacks,
                                                OrderBy<T> orderBy,
                                                SelectFunction<CriteriaBuilder,
                                                        Path<A>, Selection<A>> selectCallback) {
        return findAttribute(attributeClass, attributeName, true, conditions, fetchCallbacks, orderBy, selectCallback);
    }

    private <A> List<A> findAttribute(Class<A> attributeClass,
                                      String attributeName,
                                      boolean selectDistinct,
                                      Conditions conditions,
                                      List<Function<FetchParent<?, ?>, FetchParent<?, ?>>> fetchCallbacks,
                                      OrderBy<T> orderBy,
                                      SelectFunction<CriteriaBuilder, Path<A>, Selection<A>> selectCallback) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<A> criteriaQuery = criteriaBuilder.createQuery(attributeClass);
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

        if (orderBy != null) {
            criteriaQuery.orderBy(orderBy.apply(criteriaBuilder, root));
        }

        TypedQuery<A> typedQuery = entityManager.createQuery(criteriaQuery);

        setParameters(conditions, typedQuery);

        return typedQuery.getResultList();
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

    private abstract class Aggregate<N extends Number> {
        protected CriteriaBuilder cb;
        protected CriteriaQuery<Number> cq;
        protected Root<T> root;

        protected boolean countDistinct;

        public Aggregate(){}

        public Aggregate(boolean countDistinct){
            this.countDistinct = countDistinct;
        }

        public abstract void prepareQuery(String attributeName);

        public N calculate(Conditions conditions, String attributeName, Class<N> type){
            cb = entityManager.getCriteriaBuilder();
            cq = cb.createQuery(Number.class);
            root = cq.from(typeParameterClass);
            prepareQuery(attributeName);                                                          //TODO
            conditions.apply(cq, cb, root);
            return type.cast(conditions.setParameters(entityManager.createQuery(cq)).getSingleResult());
        }
    }

    protected <N extends Number> N min(Conditions conditions, String attributeName, Class<N> type){
        Aggregate<N> agg = new Aggregate<N>(){
            @Override
            public void prepareQuery(String attributeName){
                cq.select(cb.min(root.get(attributeName)));
            }
        };
        return agg.calculate(conditions, attributeName, type);
    }

    protected <N extends Number> N max(Conditions conditions, String attributeName, Class<N> type){
        Aggregate<N> agg = new Aggregate<N>(){
          @Override
          public void prepareQuery(String attributeName){
              cq.select(cb.max(root.get(attributeName)));
          }
        };
        return agg.calculate(conditions, attributeName, type);
    }

    protected <N extends Number> N sum(Conditions conditions, String attributeName, Class<N> type){
        Aggregate<N> agg = new Aggregate<N>(){
            @Override
            public void prepareQuery(String attributeName){
                cq.select(cb.sum(root.get(attributeName)));
            }
        };
        return agg.calculate(conditions, attributeName, type);
    }

    protected <N extends Number> N count(Conditions conditions, Class<N> type, boolean countDistinct){
        Aggregate<N> agg = new Aggregate<N>(countDistinct){
            @Override
            public void prepareQuery(String attributeName){
                if (countDistinct)
                    cq.select(cb.countDistinct(root));
                else
                    cq.select(cb.count(root));
            }
        };
        return agg.calculate(conditions, null, type);
    }

    protected long count(Conditions conditions) {
        return count(conditions, Long.class, false);
    }

    protected long countDistinct(Conditions conditions) {
        return count(conditions, Long.class, true);
    }
}
