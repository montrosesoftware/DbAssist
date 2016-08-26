package com.montrosesoftware.repositories;

import com.montrosesoftware.repositories.HierarchyCondition.Condition;
import org.hibernate.jpa.criteria.path.AbstractJoinImpl;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import javax.persistence.metamodel.Attribute;
import java.time.LocalDate;
import java.util.*;

public class ConditionsBuilder extends BaseBuilder<ConditionsBuilder> {

    @FunctionalInterface
    interface ThreeArgsFunction<A1, A2, A3, R> {
        R apply(A1 arg1, A2 arg2, A3 arg3);
    }

    private Condition whereConditions = null;

    private HashMap<String, Object> parameters = new HashMap<>();

    public ConditionsBuilder() {
    }

    private ConditionsBuilder(String joinAttribute, JoinType joinType, ConditionsBuilder parent) {
        super(joinAttribute, joinType, parent);
    }

    @Override
    public ConditionsBuilder getInstance(String joinAttribute, JoinType joinType, ConditionsBuilder parent) {
        return new ConditionsBuilder(joinAttribute, joinType, parent);
    }

    public HashMap<String, Object> getParameters() {
        return parameters;
    }

    public HierarchyCondition equal(String attributeName, String value) {
        return new HierarchyCondition(this, (cb, root) -> cb.equal(root.get(attributeName), getExpression(cb, value, String.class)));
    }

    public HierarchyCondition equal(String attributeName, Number value) {
        return new HierarchyCondition(this, (cb, root) -> cb.equal(root.get(attributeName), getExpression(cb, value, Number.class)));
    }

    public HierarchyCondition equal(String attributeName, Date value) {
        return new HierarchyCondition(this, (cb, root) -> cb.equal(root.get(attributeName), getExpression(cb, value, Date.class)));
    }

    public HierarchyCondition equal(String attributeName, LocalDate value) {
        return new HierarchyCondition(this, (cb, root) -> cb.equal(root.get(attributeName), getExpression(cb, value, LocalDate.class)));
    }

    public <T extends Comparable<T>> HierarchyCondition greaterThan(String attributeName, T value) {
        return new HierarchyCondition(this, (cb, root) -> cb.greaterThan(root.get(attributeName), getExpression(cb, value, (Class<T>) value.getClass())));
    }

    public <T extends Comparable<T>> HierarchyCondition greaterThanOrEqualTo(String attributeName, T value) {
        return new HierarchyCondition(this, (cb, root) -> cb.greaterThanOrEqualTo(root.get(attributeName), getExpression(cb, value, (Class<T>) value.getClass())));
    }

    public <T extends Comparable<T>> HierarchyCondition lessThan(String attributeName, T value) {
        return new HierarchyCondition(this, (cb, root) -> cb.lessThan(root.get(attributeName), getExpression(cb, value, (Class<T>) value.getClass())));
    }

    public <T extends Comparable<T>> HierarchyCondition lessThanOrEqualTo(String attributeName, T value) {
        return new HierarchyCondition(this, (cb, root) -> cb.lessThanOrEqualTo(root.get(attributeName), getExpression(cb, value, (Class<T>) value.getClass())));
    }

    public <T> HierarchyCondition in(String attributeName, List<T> values) {
        return new HierarchyCondition(this, (cb, root) -> getInPredicate(attributeName, values, cb, root));
    }

    public HierarchyCondition notIn(String attributeName, List<String> values) {
        return new HierarchyCondition(this, (cb, root) -> getInPredicate(attributeName, values, cb, root).not());
    }

    public HierarchyCondition like(String attributeName, String value) {
        return new HierarchyCondition(this, (cb, root) -> cb.like(root.get(attributeName), getExpression(cb, value, String.class)));
    }

    public HierarchyCondition notLike(String attributeName, String value) {
        return new HierarchyCondition(this, (cb, root) -> cb.like(root.get(attributeName), getExpression(cb, value, String.class)).not());
    }

    public HierarchyCondition isNull(String attributeName) {
        return new HierarchyCondition(this, (cb, root) -> root.get(attributeName).isNull());
    }

    public HierarchyCondition isNotNull(String attributeName) {
        return new HierarchyCondition(this, (cb, root) -> root.get(attributeName).isNotNull());
    }

    public Condition apply(HierarchyCondition hierarchyCondition) {
        whereConditions = hierarchyCondition.apply(this);
        return whereConditions;
    }

    public ConditionsBuilder join(String joinAttribute, JoinType joinType) {
        return getBuilder(joinAttribute, joinType);
    }

    public static HierarchyCondition and(HierarchyCondition hcLeft, HierarchyCondition hcRight) {
        return new HierarchyCondition(hcLeft, hcRight, HierarchyCondition.LogicalOperator.AND);
    }

    public static HierarchyCondition or(HierarchyCondition hcLeft, HierarchyCondition hcRight) {
        return new HierarchyCondition(hcLeft, hcRight, HierarchyCondition.LogicalOperator.OR);
    }

    public static HierarchyCondition and(List<HierarchyCondition> conditions) {

        if (conditions.size() == 1) {
            return conditions.get(0);
        } else if (conditions.size() == 2) {
            return and(conditions.get(0), conditions.get(1));
        } else {
            List<HierarchyCondition> subConditions = new LinkedList<>();
            subConditions.add(and(conditions.get(0), conditions.get(1)));
            subConditions.addAll(1, conditions.subList(2, conditions.size()));
            return and(subConditions);
        }
    }

    public static HierarchyCondition or(List<HierarchyCondition> conditions) {

        if (conditions.size() == 1) {
            return conditions.get(0);
        } else if (conditions.size() == 2) {
            return or(conditions.get(0), conditions.get(1));
        } else {
            List<HierarchyCondition> subConditions = new LinkedList<>();
            subConditions.add(or(conditions.get(0), conditions.get(1)));
            subConditions.addAll(1, conditions.subList(2, conditions.size()));
            return or(subConditions);
        }
    }

    public Condition applyLogicalOperator(Condition leftOperandCondition,
                                          Condition rightOperandCondition,
                                          ThreeArgsFunction<CriteriaBuilder, Predicate, Predicate, Predicate> logicalOperator) {

        if (leftOperandCondition == null || rightOperandCondition == null) {
            return null;
        }

        ConditionsBuilder leftOperandJoinConditionsBuilder = leftOperandCondition.getConditionsBuilder();
        ConditionsBuilder rightOperandJoinConditionsBuilder = rightOperandCondition.getConditionsBuilder();

        HierarchyCondition.ApplicableCondition applicableCondition = (cb, root) -> {
            From<?, ?> leftFrom = getFrom(root, leftOperandJoinConditionsBuilder);
            From<?, ?> rightFrom = getFrom(root, rightOperandJoinConditionsBuilder);

            return logicalOperator.apply(cb, leftOperandCondition.getApplicableCondition().apply(cb, leftFrom), rightOperandCondition.getApplicableCondition().apply(cb, rightFrom));
        };

        return new Condition(this, applicableCondition);
    }

    private <T> Predicate getInPredicate(String attributeName, List<T> values, CriteriaBuilder cb, From<?, ?> root) {
        return root.get(attributeName).in(getExpression(cb, values, List.class));
    }

    private Condition assignWhereConditionsAndReturn(Condition condition) {
        whereConditions = condition;
        return condition;
    }

    public <T extends Comparable<T>> HierarchyCondition inRangeCondition(String attributeName, T leftBound, T rightBound) {
        return and(
                this.greaterThanOrEqualTo(attributeName, leftBound),
                this.lessThanOrEqualTo(attributeName, rightBound)
        );
    }

    public <T extends Comparable<T>> HierarchyCondition inRangeExclusiveCondition(String attributeName, T leftBound, T rightBound) {
        return and(
                this.greaterThan(attributeName, leftBound),
                this.lessThan(attributeName, rightBound)
        );
    }

    public void applyConditions(CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder, Root<?> root) {
        if (!this.getParameters().isEmpty())
            throw new RuntimeException("The conditions were already used.");

        applyPredicate(getPredicate(criteriaQuery, criteriaBuilder, root), criteriaQuery, criteriaBuilder);
    }

    public TypedQuery<?> setParameters(TypedQuery<?> typedQuery) {
        parameters.forEach(typedQuery::setParameter);

        if (builders != null && !builders.isEmpty()) {
            builders.forEach((joinAttribute, joinCondition) -> joinCondition.setParameters(typedQuery));
        }

        return typedQuery;
    }

    private Predicate getPredicate(CriteriaQuery<?> query, CriteriaBuilder cb, From<?, ?> root) {
        Predicate predicate = null;
        if (whereConditions != null)
            predicate = whereConditions.getApplicableCondition().apply(cb, root);

        return predicate;
    }

    private CriteriaQuery<?> applyPredicate(Predicate predicate, CriteriaQuery<?> query, CriteriaBuilder cb) {
        return predicate == null ? query : query.where(cb.and(predicate));
    }

    private <T> ParameterExpression<T> getExpression(CriteriaBuilder cb, Object value, Class<T> typeParameterClass) {
        String name = getRandomParamName();
        parameters.put(name, value);

        return cb.parameter(typeParameterClass, name);
    }

    private String getRandomParamName() {
        return "param" + UUID.randomUUID().toString().replaceAll("-", "").replaceAll("\\d", "");
    }

    private From<?, ?> checkAndGetExistingJoinOrFetch(From<?, ?> from, ConditionsBuilder joinConditionBuilder) {
        FetchParent<?, ?> fetchParent = checkExisting(joinConditionBuilder, from.getJoins());
        fetchParent = fetchParent != null ? fetchParent : checkExisting(joinConditionBuilder, from.getFetches());
        return (From<?, ?>) fetchParent;
    }

    private From<?, ?> safeJoin(From<?, ?> from, ConditionsBuilder joinConditionBuilder) {
        FetchParent<?, ?> fetchParent = checkAndGetExistingJoinOrFetch(from, joinConditionBuilder);
        fetchParent = fetchParent != null ? fetchParent : from.join(joinConditionBuilder.joinAttribute, joinConditionBuilder.joinType);
        return (From<?, ?>) fetchParent;
    }

    private From<?, ?> getFrom(From<?, ?> rootFrom, ConditionsBuilder joinConditionBuilder) {
        if (joinConditionBuilder == null)
            throw new RuntimeException("joinConditionsBuilder should be not null");

        if (joinConditionBuilder == this)
            return rootFrom;

        From<?, ?> from = checkAndGetExistingJoinOrFetch(rootFrom, joinConditionBuilder);
        if (from == null) {
            From<?, ?> previousFrom = getPreviousFrom(rootFrom, joinConditionBuilder);
            from = safeJoin(previousFrom, joinConditionBuilder);
        }

        return from;
    }

    public From<?, ?> getPreviousFrom(From<?, ?> from, ConditionsBuilder conditionsBuilder) {

        From<?, ?> newFrom = null;

        ConditionsBuilder parentBuilder = conditionsBuilder.getParent();

        if (this == parentBuilder) {
            return from;
        }

        if (parentBuilder != null) {
            if (this.getBuilders().containsValue(parentBuilder)) {
                //make join
                newFrom = safeJoin(from, parentBuilder);
            } else {
                //recursive
                From<?, ?> previousFrom = getPreviousFrom(from, parentBuilder);
                newFrom = safeJoin(previousFrom, parentBuilder);
            }
        }

        return newFrom;
    }

    private FetchParent<?, ?> checkExisting(ConditionsBuilder joinCondition, Set<?> joinsOrFetches) {
        // Method is called to verify if a join or fetch was already made. In such case return existing join
        // It is necessary to prevent duplicated SQL code for the same joins, when for example applying logical operators on conditions

        FetchParent<?, ?> fetchParent = null;

        if (!joinsOrFetches.isEmpty()) {

            for (Object existingSingularAttribute : joinsOrFetches) {
                AbstractJoinImpl<?, ?> existingSingularAttributeJoin = (AbstractJoinImpl<?, ?>) existingSingularAttribute;
                Attribute<?, ?> joinAttribute = existingSingularAttributeJoin.getAttribute();

                if (joinAttribute.getName().equals(joinCondition.joinAttribute)) {
                    fetchParent = existingSingularAttributeJoin;
                    break;
                }
            }
        }

        return fetchParent;
    }
}
