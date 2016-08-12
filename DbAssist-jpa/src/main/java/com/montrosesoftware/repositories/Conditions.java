package com.montrosesoftware.repositories;

import org.hibernate.jpa.criteria.path.AbstractJoinImpl;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import javax.persistence.metamodel.Attribute;
import java.time.LocalDate;
import java.util.*;

public class Conditions {

    public interface Condition {
        Predicate apply(CriteriaBuilder cb, From<?, ?> root);
    }

    @FunctionalInterface
    interface ThreeArgsFunction<A1, A2, A3, R> {
        R apply(A1 arg1, A2 arg2, A3 arg3);
    }

    // Per single join
    private HashMap<String, Conditions> joinConditions = new HashMap<>();

    private LinkedList<Condition> whereConditions = new LinkedList<>();

    private HashMap<String, Object> parameters = new HashMap<>();

    private String joinAttribute;

    private JoinType joinType;

    public Conditions() {}

    //TODO find a better way to do it
    public Conditions(Conditions another){
        this.joinConditions = new HashMap<>(another.joinConditions);
        this.whereConditions = new LinkedList<>(another.whereConditions);
        this.parameters = new HashMap<>(parameters);
        this.joinAttribute = new String(another.joinAttribute);
        this.joinType = another.joinType;
    }

    private Conditions(String joinAttribute, JoinType joinType) {
        this.joinAttribute = joinAttribute;
        this.joinType = joinType;
    }

    public Condition equal(String attributeName, String value) {
        return addToWhereConditionsAndReturn((cb, root) -> cb.equal(root.get(attributeName), getExpression(cb, value, String.class)));
    }

    public Condition equal(String attributeName, Number value) {
        return addToWhereConditionsAndReturn((cb, root) -> cb.equal(root.get(attributeName), getExpression(cb, value, Number.class)));
    }

    public Condition equal(String attributeName, Date value) {
        return addToWhereConditionsAndReturn((cb, root) -> cb.equal(root.get(attributeName), getExpression(cb, value, Date.class)));
    }

    public Condition equal(String attributeName, LocalDate value) {
        return addToWhereConditionsAndReturn((cb, root) -> cb.equal(root.get(attributeName), getExpression(cb, value, LocalDate.class)));
    }

    public <T extends Comparable<T>> Condition greaterThan(String attributeName, T value){
        return addToWhereConditionsAndReturn((cb, root) -> cb.greaterThan(root.get(attributeName), getExpression(cb, value, (Class<T>)value.getClass())));
    }

    public <T extends Comparable<T>> Condition greaterThanOrEqualTo(String attributeName, T value){
        return addToWhereConditionsAndReturn((cb, root) -> cb.greaterThanOrEqualTo(root.get(attributeName), getExpression(cb, value, (Class<T>)value.getClass())));
    }

    public <T extends Comparable<T>> Condition lessThan(String attributeName, T value){
        return addToWhereConditionsAndReturn((cb, root) -> cb.lessThan(root.get(attributeName), getExpression(cb, value, (Class<T>)value.getClass())));
    }

    public <T extends Comparable<T>> Condition lessThanOrEqualTo(String attributeName, T value){
        return addToWhereConditionsAndReturn((cb, root) -> cb.lessThanOrEqualTo(root.get(attributeName), getExpression(cb, value, (Class<T>)value.getClass())));
    }

    public <T> Condition in(String attributeName, List<T> values) {
        return addToWhereConditionsAndReturn((cb, root) -> getInPredicate(attributeName, values, cb, root));
    }

    public Condition notIn(String attributeName, List<String> values) {
        return addToWhereConditionsAndReturn((cb, root) -> getInPredicate(attributeName, values, cb, root).not());
    }

    public Condition notLike(String attributeName, String value) {
        return addToWhereConditionsAndReturn((cb, root) -> cb.like(root.get(attributeName), getExpression(cb, value, String.class)).not());
    }

    public Condition isNull(String attributeName) {
        return addToWhereConditionsAndReturn((cb, root) -> root.get(attributeName).isNull());
    }

    public Condition isNotNull(String attributeName) {
        return addToWhereConditionsAndReturn((cb, root) -> root.get(attributeName).isNotNull());
    }

    public Condition or(Condition leftOperandCondition, Conditions rightOperandJoinConditions, Condition rightOperandCondition) {
        return or(this, leftOperandCondition, rightOperandJoinConditions, rightOperandCondition);
    }

    public Condition or(Conditions leftOperandJoinConditions, Condition leftOperandCondition, Conditions rightOperandJoinConditions, Condition rightOperandCondition) {
        return applyLogicalOperator(leftOperandJoinConditions, leftOperandCondition, rightOperandJoinConditions, rightOperandCondition, (cb, p1, p2) -> cb.or(p1, p2));
    }

    public Condition and(Condition leftOperandCondition, Conditions rightOperandJoinConditions, Condition rightOperandCondition) {
        return applyLogicalOperator(this, leftOperandCondition, rightOperandJoinConditions, rightOperandCondition, (cb, p1, p2) -> cb.and(p1, p2));
    }

    private Condition applyLogicalOperator(Conditions leftOperandJoinConditions,
                                           Condition leftOperandCondition,
                                           Conditions rightOperandJoinConditions,
                                           Condition rightOperandCondition,
                                           ThreeArgsFunction<CriteriaBuilder, Predicate, Predicate, Predicate> logicalOperator) {
        if (leftOperandCondition == null || rightOperandCondition == null) {
            return null;
        }

        if (leftOperandJoinConditions != null && leftOperandJoinConditions.whereConditions.contains(leftOperandCondition)) {
            leftOperandJoinConditions.whereConditions.remove(leftOperandCondition);
        }

        if (leftOperandJoinConditions != null && leftOperandJoinConditions.whereConditions.contains(rightOperandCondition)) {
            leftOperandJoinConditions.whereConditions.remove(rightOperandCondition);
        }

        if (rightOperandJoinConditions != null && rightOperandJoinConditions.whereConditions.contains(leftOperandCondition)) {
            rightOperandJoinConditions.whereConditions.remove(leftOperandCondition);
        }

        if (rightOperandJoinConditions != null && rightOperandJoinConditions.whereConditions.contains(rightOperandCondition)) {
            rightOperandJoinConditions.whereConditions.remove(rightOperandCondition);
        }

        Condition condition = (cb, root) -> {
            From<?, ?> leftFrom = getFrom(root, leftOperandJoinConditions);
            From<?, ?> rightFrom = getFrom(root, rightOperandJoinConditions);

            return logicalOperator.apply(cb, leftOperandCondition.apply(cb, leftFrom), rightOperandCondition.apply(cb, rightFrom));
        };

        return addToWhereConditionsAndReturn(condition);
    }

    private <T> Predicate getInPredicate(String attributeName, List<T> values, CriteriaBuilder cb, From<?, ?> root) {
        return root.get(attributeName).in(getExpression(cb, values, List.class));
    }

    private Condition addToWhereConditionsAndReturn(Condition condition) {
        whereConditions.add(condition);

        return condition;
    }

    public Conditions getJoinConditions(String joinAttribute, JoinType joinType) {
        Conditions conditions = joinConditions.get(joinAttribute);

        if (conditions == null) {
            conditions = new Conditions(joinAttribute, joinType);
            joinConditions.put(joinAttribute, conditions);
        }

        return conditions;
    }

    public <T extends Comparable<T>> void inRangeConditions(String attributeName, T leftBound, T rightBound){
        this.and(
                this.greaterThanOrEqualTo(attributeName, leftBound),
                null,
                this.lessThanOrEqualTo(attributeName, rightBound)
        );
    }

    public void apply(CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder, Root<?> root) {
        applyPredicates(getPredicates(criteriaQuery, criteriaBuilder, root), criteriaQuery, criteriaBuilder);
    }

    public TypedQuery<?> setParameters(TypedQuery<?> typedQuery) {
        parameters.forEach(typedQuery::setParameter);

        if (joinConditions != null && !joinConditions.isEmpty()) {
            joinConditions.forEach((joinAttribute, joinCondition) -> joinCondition.setParameters(typedQuery));
        }

        return typedQuery;
    }

    private List<Predicate> getPredicates(CriteriaQuery<?> query, CriteriaBuilder cb, From<?, ?> root) {
        List<Predicate> predicates = new LinkedList<Predicate>();

        whereConditions.forEach(condition -> predicates.add(condition.apply(cb, root)));

        if (joinConditions != null && !joinConditions.isEmpty()) {
            joinConditions.forEach((joinAttribute, joinCondition) -> {
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
        String name = getRandomName();
        parameters.put(name, value);

        return cb.parameter(typeParameterClass, name);
    }

    private String getRandomName() {
        return UUID.randomUUID().toString().replaceAll("-", "").replaceAll("\\d", "");
    }

    private From<?, ?> getFrom(From<?, ?> from, Conditions joinCondition) {
        if (joinCondition == null || joinCondition == this) {
            return from;
        }

        FetchParent<?, ?> fetchParent = null;

        fetchParent = checkExisting(joinCondition, fetchParent, from.getJoins());
        fetchParent = fetchParent != null ? fetchParent : checkExisting(joinCondition, fetchParent, from.getFetches());
        fetchParent = fetchParent != null ? fetchParent : from.join(joinCondition.joinAttribute, joinCondition.joinType);

        return (From<?, ?>) fetchParent;
    }

    private FetchParent<?, ?> checkExisting(Conditions joinCondition, FetchParent<?, ?> fetchParent, Set<?> joinsOrFetches) {
        if (!joinsOrFetches.isEmpty()) {
            LinkedHashSet<?> existingSingularAttributes = (LinkedHashSet<?>) joinsOrFetches;
            Iterator<?> itor = existingSingularAttributes.iterator();

            while (itor.hasNext()) {
                AbstractJoinImpl<?, ?> existingSingularAttributeJoin = (AbstractJoinImpl<?, ?>) itor.next();
                Attribute<?, ?> joinAttribute = existingSingularAttributeJoin.getAttribute();

                if (joinAttribute.getName().equals(joinCondition.joinAttribute)) {
                    fetchParent = existingSingularAttributeJoin;
                }
            }
        }

        return fetchParent;
    }
}
