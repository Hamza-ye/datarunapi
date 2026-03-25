package org.nmcpye.datarun.sharedkernal.apiquery;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import org.nmcpye.datarun.sharedkernal.apiquery.filter.CompoundFilter;
import org.nmcpye.datarun.sharedkernal.apiquery.filter.FilterExpression;
import org.nmcpye.datarun.sharedkernal.apiquery.filter.LogicalOperator;
import org.nmcpye.datarun.sharedkernal.apiquery.filter.SimpleFilter;
import org.nmcpye.datarun.sharedkernal.exceptions.IllegalQueryException;
import org.nmcpye.datarun.sharedkernal.feedback.ErrorCode;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Hamza Assada 23/03/2025 (7amza.it@gmail.com)
 */
@Component("jpaQueryBuilder")
public class JpaQueryBuilder<T> implements QueryBuilder<Specification<T>> {

    @Override
    public Specification<T> buildQuery(List<FilterExpression> expressions) {
        return (root, query, cb) -> buildPredicate(root, cb, new CompoundFilter(LogicalOperator.AND, expressions));
    }

    private Predicate buildPredicate(Root<T> root, CriteriaBuilder cb, FilterExpression expression) {
        if (expression instanceof SimpleFilter simple) {
            Path<Object> path = resolvePath(root, simple.getField());
            return switch (simple.getOperator()) {
                case EQ -> cb.equal(path, simple.getValue());
                case NE -> cb.notEqual(path, simple.getValue());
                case GT -> cb.gt(path.as(Number.class), (Number) simple.getValue());
                case GTE -> cb.ge(path.as(Number.class), (Number) simple.getValue());
                case LT -> cb.lt(path.as(Number.class), (Number) simple.getValue());
                case LTE -> cb.le(path.as(Number.class), (Number) simple.getValue());
                case IN -> path.in((List<?>) simple.getValue());
                case EXISTS -> simple.getValue().equals(Boolean.TRUE)
                    ? cb.isNotNull(path) : cb.isNull(path);
                case REGEX -> cb.like(path.as(String.class), "%" + simple.getValue() + "%");
                case NULL -> cb.isNull(path);
            };
        } else if (expression instanceof CompoundFilter compound) {
            List<Predicate> predicates = compound.getExpressions().stream()
                .map(expr -> buildPredicate(root, cb, expr))
                .toList();
            return compound.getLogicalOperator() == LogicalOperator.AND
                ? cb.and(predicates.toArray(new Predicate[0]))
                : cb.or(predicates.toArray(new Predicate[0]));
        }
        throw new IllegalQueryException(ErrorCode.E2019, expression);
    }

    private Path<Object> resolvePath(Root<T> root, String field) {
        // Handle nested properties, for example: parent.uid
        if (field.contains(".")) {
            String[] parts = field.split("\\.");
            Path<Object> path = root.get(parts[0]);
            for (int i = 1; i < parts.length; i++) {
                path = path.get(parts[i]);
            }
            return path;
        } else {
            return root.get(field);
        }
    }
}
