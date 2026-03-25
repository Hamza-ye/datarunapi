package org.nmcpye.datarun.sharedkernal.apiquery;

import org.nmcpye.datarun.sharedkernal.apiquery.filter.*;
import org.nmcpye.datarun.sharedkernal.feedback.ErrorCode;
import org.nmcpye.datarun.sharedkernal.feedback.ErrorMessage;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @author Hamza Assada 01/06/2025 (7amza.it@gmail.com)
 */
@Component
public class LegacyQueryConverter {
    public FilterExpression convert(QueryRequest queryRequest) {
        if (queryRequest.getParsedFilter() == null || queryRequest.getParsedFilter().isEmpty()) {
            return null;
        }

        List<FilterExpression> expressions = queryRequest.getParsedFilter().entrySet().stream()
            .map(this::toExpression)
            .toList();

        return new CompoundFilter(LogicalOperator.AND, expressions);
    }

    private FilterExpression toExpression(Map.Entry<String, Object> entry) {
        String key = entry.getKey();
        Object value = entry.getValue();
        String[] parts = key.split("__");
        String field = parts[0];
        String operator = parts.length > 1 ? parts[1] : "eq";

        return switch (operator) {
            case "eq" -> new SimpleFilter(field, FilterOperator.EQ, value);
            case "ne" -> new SimpleFilter(field, FilterOperator.NE, value);
            case "gt" -> new SimpleFilter(field, FilterOperator.GT, value);
            case "gte" -> new SimpleFilter(field, FilterOperator.GTE, value);
            case "lt" -> new SimpleFilter(field, FilterOperator.LT, value);
            case "lte" -> new SimpleFilter(field, FilterOperator.LTE, value);
            case "regex" -> new SimpleFilter(field, FilterOperator.REGEX, value);
            case "in" -> new SimpleFilter(field, FilterOperator.IN, toList(value));
            case "exists" -> new SimpleFilter(field, FilterOperator.EXISTS, toBoolean(value));
            default -> throw new QueryRequestValidationException(new ErrorMessage(ErrorCode.E2035, operator));
        };
    }

    private List<Object> toList(Object val) {
        if (val instanceof List<?> l) return new ArrayList<>(l);
        if (val instanceof String s)
            return Collections.singletonList(Arrays.stream(s.split(",")).map(String::trim).toList());
        return List.of(val);
    }

    private boolean toBoolean(Object value) {
        if (value instanceof Boolean) return (Boolean) value;
        if (value instanceof String) return Boolean.parseBoolean((String) value);
        throw new QueryRequestValidationException(new ErrorMessage(ErrorCode.E2035, value));
    }
}
