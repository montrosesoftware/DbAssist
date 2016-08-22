package com.montrosesoftware.repositories;

import org.hibernate.jpa.criteria.path.AbstractJoinImpl;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import javax.persistence.metamodel.Attribute;
import java.time.LocalDate;
import java.util.*;

public class ConditionsBuilder {

    public interface ApplicableCondition {
        Predicate apply(CriteriaBuilder cb, From<?, ?> root);
    }

    public class Condition {
        private ApplicableCondition applicableCondition;
        private ConditionsBuilder conditionsBuilder;

        public Condition(ConditionsBuilder conditionsBuilder, ApplicableCondition applicableCondition) {
            this.applicableCondition = applicableCondition;
            this.conditionsBuilder = conditionsBuilder;
        }

        public ApplicableCondition getApplicableCondition() {
            return applicableCondition;
        }

        public ConditionsBuilder getConditionsBuilder() {
            return conditionsBuilder;
        }
    }

    @FunctionalInterface
    interface ThreeArgsFunction<A1, A2, A3, R> {
        R apply(A1 arg1, A2 arg2, A3 arg3);
    }

    // Per single join
    private HashMap<String, ConditionsBuilder> joinConditionsBuilders = new HashMap<>();

    private HashMap<String, ConditionsBuilder> getJoinConditionsBuilders() {
        return joinConditionsBuilders;
    }

    private ConditionsBuilder joinParent = null;

    private LinkedList<Condition> whereConditions = new LinkedList<>();

    private HashMap<String, Object> parameters = new HashMap<>();

    private String joinAttribute;

    private JoinType joinType;

    public ConditionsBuilder() {}

    private ConditionsBuilder(String joinAttribute, JoinType joinType) {
        this.joinAttribute = joinAttribute;
        this.joinType = joinType;
    }

    public ConditionsBuilder getJoinParent() {
        return joinParent;
    }

    public void setJoinParent(ConditionsBuilder joinParent) {
        this.joinParent = joinParent;
    }

    public HashMap<String, Object> getParameters() {
        return parameters;
    }

    public Condition equal(String attributeName, String value) {
        return addToWhereConditionsAndReturn(new Condition(this, (cb, root) -> cb.equal(root.get(attributeName), getExpression(cb, value, String.class))));
    }

    public Condition equal(String attributeName, Number value) {
        return addToWhereConditionsAndReturn(new Condition(null, (cb, root) -> cb.equal(root.get(attributeName), getExpression(cb, value, Number.class))));
    }

    public Condition equal(String attributeName, Date value) {
        return addToWhereConditionsAndReturn(new Condition(this, (cb, root) -> cb.equal(root.get(attributeName), getExpression(cb, value, Date.class))));
    }

    public Condition equal(String attributeName, LocalDate value) {
        return addToWhereConditionsAndReturn(new Condition(this, (cb, root) -> cb.equal(root.get(attributeName), getExpression(cb, value, LocalDate.class))));
    }

    public <T extends Comparable<T>> Condition greaterThan(String attributeName, T value){
        return addToWhereConditionsAndReturn(new Condition(this, (cb, root) -> cb.greaterThan(root.get(attributeName), getExpression(cb, value, (Class<T>)value.getClass()))));
    }

    public <T extends Comparable<T>> Condition greaterThanOrEqualTo(String attributeName, T value){
        return addToWhereConditionsAndReturn(new Condition(this, (cb, root) -> cb.greaterThanOrEqualTo(root.get(attributeName), getExpression(cb, value, (Class<T>)value.getClass()))));
    }

    public <T extends Comparable<T>> Condition lessThan(String attributeName, T value){
        return addToWhereConditionsAndReturn(new Condition(this, (cb, root) -> cb.lessThan(root.get(attributeName), getExpression(cb, value, (Class<T>)value.getClass()))));
    }

    public <T extends Comparable<T>> Condition lessThanOrEqualTo(String attributeName, T value){
        return addToWhereConditionsAndReturn(new Condition(this, (cb, root) -> cb.lessThanOrEqualTo(root.get(attributeName), getExpression(cb, value, (Class<T>)value.getClass()))));
    }

    public <T> Condition in(String attributeName, List<T> values) {
        return addToWhereConditionsAndReturn(new Condition(this, (cb, root) -> getInPredicate(attributeName, values, cb, root)));
    }

    public Condition notIn(String attributeName, List<String> values) {
        return addToWhereConditionsAndReturn(new Condition(this, (cb, root) -> getInPredicate(attributeName, values, cb, root).not()));
    }

    public Condition like(String attributeName, String value) {
        return addToWhereConditionsAndReturn(new Condition(this, (cb, root) -> cb.like(root.get(attributeName), getExpression(cb, value, String.class))));
    }

    public Condition notLike(String attributeName, String value) {
        return addToWhereConditionsAndReturn(new Condition(this, (cb, root) -> cb.like(root.get(attributeName), getExpression(cb, value, String.class)).not()));
    }

    public Condition isNull(String attributeName) {
        return addToWhereConditionsAndReturn(new Condition(this, (cb, root) -> root.get(attributeName).isNull()));
    }

    public Condition isNotNull(String attributeName) {
        return addToWhereConditionsAndReturn(new Condition(this, (cb, root) -> root.get(attributeName).isNotNull()));
    }

    public Condition or(Condition leftOperandCondition, Condition rightOperandCondition) {
        return applyLogicalOperator(leftOperandCondition, rightOperandCondition, (cb, p1, p2) -> cb.or(p1, p2));
    }

//    public Condition or(ApplicableCondition leftOperandApplicableCondition, ConditionsBuilder rightOperandJoinConditionsBuilder, ApplicableCondition rightOperandApplicableCondition) {
//        return or(this, leftOperandApplicableCondition, rightOperandJoinConditionsBuilder, rightOperandApplicableCondition);
//    }
//
//    public Condition or(ConditionsBuilder leftOperandJoinConditionsBuilder, ApplicableCondition leftOperandApplicableCondition, ConditionsBuilder rightOperandJoinConditionsBuilder, ApplicableCondition rightOperandApplicableCondition) {
//        return applyLogicalOperator(leftOperandJoinConditionsBuilder, leftOperandApplicableCondition, rightOperandJoinConditionsBuilder, rightOperandApplicableCondition, (cb, p1, p2) -> cb.or(p1, p2));
//    }

    public Condition and(Condition leftOperandCondition, Condition rightOperandCondition) {
        return applyLogicalOperator(leftOperandCondition, rightOperandCondition, (cb, p1, p2) -> cb.and(p1, p2));

    }

//    public Condition and(ApplicableCondition leftOperandApplicableCondition, ConditionsBuilder rightOperandJoinConditionsBuilder, ApplicableCondition rightOperandApplicableCondition) {
//        return applyLogicalOperator(this, leftOperandApplicableCondition, rightOperandJoinConditionsBuilder, rightOperandApplicableCondition, (cb, p1, p2) -> cb.and(p1, p2));
//    }

    private Condition applyLogicalOperator(Condition leftOperandCondition,
                                           Condition rightOperandCondition,
                                           ThreeArgsFunction<CriteriaBuilder, Predicate, Predicate, Predicate> logicalOperator) {

        if (leftOperandCondition == null || rightOperandCondition == null) {
            return null;
        }

        ConditionsBuilder leftOperandJoinConditionsBuilder = leftOperandCondition.getConditionsBuilder();
        ConditionsBuilder rightOperandJoinConditionsBuilder = rightOperandCondition.getConditionsBuilder();

        if (leftOperandJoinConditionsBuilder != null && leftOperandJoinConditionsBuilder.whereConditions.contains(leftOperandCondition)) {
            leftOperandJoinConditionsBuilder.whereConditions.remove(leftOperandCondition);
        }

        if (leftOperandJoinConditionsBuilder != null && leftOperandJoinConditionsBuilder.whereConditions.contains(rightOperandCondition)) {
            leftOperandJoinConditionsBuilder.whereConditions.remove(rightOperandCondition);
        }

        if (rightOperandJoinConditionsBuilder != null && rightOperandJoinConditionsBuilder.whereConditions.contains(leftOperandCondition)) {
            rightOperandJoinConditionsBuilder.whereConditions.remove(leftOperandCondition);
        }

        if (rightOperandJoinConditionsBuilder != null && rightOperandJoinConditionsBuilder.whereConditions.contains(rightOperandCondition)) {
            rightOperandJoinConditionsBuilder.whereConditions.remove(rightOperandCondition);
        }

        ApplicableCondition applicableCondition = (cb, root) -> {
            From<?, ?> leftFrom = getFrom(root, leftOperandJoinConditionsBuilder);
            From<?, ?> rightFrom = getFrom(root, rightOperandJoinConditionsBuilder);

            return logicalOperator.apply(cb, leftOperandCondition.getApplicableCondition().apply(cb, leftFrom), rightOperandCondition.getApplicableCondition().apply(cb, rightFrom));
        };

        return addToWhereConditionsAndReturn(new Condition(this, applicableCondition));
    }

    private <T> Predicate getInPredicate(String attributeName, List<T> values, CriteriaBuilder cb, From<?, ?> root) {
        return root.get(attributeName).in(getExpression(cb, values, List.class));
    }

    private Condition addToWhereConditionsAndReturn(Condition condition) {
        whereConditions.add(condition);

        return condition;
    }

    public ConditionsBuilder getJoinConditionsBuilder(String joinAttribute, JoinType joinType) {
        ConditionsBuilder conditionsBuilder = joinConditionsBuilders.get(joinAttribute);

        if (conditionsBuilder == null) {
            conditionsBuilder = new ConditionsBuilder(joinAttribute, joinType);
            conditionsBuilder.setJoinParent(this);
            joinConditionsBuilders.put(joinAttribute, conditionsBuilder);
        }

        return conditionsBuilder;
    }

    public <T extends Comparable<T>> Condition inRangeCondition(String attributeName, T leftBound, T rightBound){
        return this.and(
                this.greaterThanOrEqualTo(attributeName, leftBound),
                this.lessThanOrEqualTo(attributeName, rightBound)
        );
    }

    public <T extends Comparable<T>> Condition inRangeExclusiveCondition(String attributeName, T leftBound, T rightBound){
        return this.and(
                this.greaterThan(attributeName, leftBound),
                this.lessThan(attributeName, rightBound)
        );
    }

    public void apply(CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder, Root<?> root) {
        if(!this.getParameters().isEmpty())
            throw new RuntimeException("The conditions were already used.");

        applyPredicates(getPredicates(criteriaQuery, criteriaBuilder, root), criteriaQuery, criteriaBuilder);
    }

    public TypedQuery<?> setParameters(TypedQuery<?> typedQuery) {
        parameters.forEach(typedQuery::setParameter);

        if (joinConditionsBuilders != null && !joinConditionsBuilders.isEmpty()) {
            joinConditionsBuilders.forEach((joinAttribute, joinCondition) -> joinCondition.setParameters(typedQuery));
        }

        return typedQuery;
    }

    private List<Predicate> getPredicates(CriteriaQuery<?> query, CriteriaBuilder cb, From<?, ?> root) {
        List<Predicate> predicates = new LinkedList<Predicate>();

        whereConditions.forEach(condition -> predicates.add(condition.getApplicableCondition().apply(cb, root)));

        if (joinConditionsBuilders != null && !joinConditionsBuilders.isEmpty()) {
            joinConditionsBuilders.forEach((joinAttribute, joinCondition) -> {
                From<?, ?> from = getFrom(root, joinCondition);
                predicates.addAll(joinCondition.getPredicates(query, cb, from));
            });
        }

        return predicates;
    }

    private CriteriaQuery<?> applyPredicates(List<Predicate> predicates, CriteriaQuery<?> query, CriteriaBuilder cb) {
        return query.where(cb.and(predicates.toArray(new Predicate[predicates.size()])));
    }

    private <T> ParameterExpression<T> getExpression(CriteriaBuilder cb, Object value, Class<T> typeParameterClass) {
        String name = getRandomParamName();
        parameters.put(name, value);

        return cb.parameter(typeParameterClass, name);
    }

    private String getRandomParamName() {
        return "param" + UUID.randomUUID().toString().replaceAll("-", "").replaceAll("\\d", "");
    }

    public From<?, ?> getPrevious(From<?, ?> from, ConditionsBuilder conditionsBuilder){

        FetchParent<?, ?> fetchParent = null;

        ConditionsBuilder parentBuilder = conditionsBuilder.getJoinParent();
        if(this == parentBuilder){
            return from;
        } else{
            if (parentBuilder != null) {

                if (this.getJoinConditionsBuilders().containsValue(parentBuilder)) {
                    //make join
                    fetchParent = from.join(parentBuilder.joinAttribute, parentBuilder.joinType);
                }else{
                    //recursive
                    From<?, ?> currentFrom = getPrevious(from, parentBuilder);
                    fetchParent = currentFrom.join(conditionsBuilder.joinAttribute, conditionsBuilder.joinType); //TODO rethink
                }
            }
        }

        return (From<?, ?>) fetchParent;
    }

    private From<?, ?> getFrom(From<?, ?> from, ConditionsBuilder joinConditionBuilder) {
        if (joinConditionBuilder == null || joinConditionBuilder == this) {   //TODO THIS or not
            return from;
        }

        FetchParent<?, ?> fetchParent = null;

        From<?,?> previousFrom = ( From<?,?> ) getPrevious(from, joinConditionBuilder);
        fetchParent = previousFrom.join(joinConditionBuilder.joinAttribute, joinConditionBuilder.joinType);

        return (From<?, ?>) fetchParent;
    }

    private FetchParent<?, ?> checkExisting(ConditionsBuilder joinCondition, FetchParent<?, ?> fetchParent, Set<?> joinsOrFetches) {
        if (!joinsOrFetches.isEmpty()) {
            LinkedHashSet<?> existingSingularAttributes = (LinkedHashSet<?>) joinsOrFetches;

            for (Object existingSingularAttribute : existingSingularAttributes) {
                AbstractJoinImpl<?, ?> existingSingularAttributeJoin = (AbstractJoinImpl<?, ?>) existingSingularAttribute;
                Attribute<?, ?> joinAttribute = existingSingularAttributeJoin.getAttribute();

                if (joinAttribute.getName().equals(joinCondition.joinAttribute)) {
                    fetchParent = existingSingularAttributeJoin;
                }
            }
        }

        return fetchParent;
    }


}
