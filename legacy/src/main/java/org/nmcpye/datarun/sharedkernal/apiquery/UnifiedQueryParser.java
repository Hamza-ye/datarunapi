package org.nmcpye.datarun.sharedkernal.apiquery;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.nmcpye.datarun.sharedkernal.apiquery.filter.*;

/**
 * represent {@link SimpleFilter} and {@link CompoundFilter}
 * for both Jpa entities, For example,
 *
 * <pre>{@code
 *      {"name": {"$eq": "ANC"}}
 * }</pre>
 *
 * <blockquote>
 * <pre>{@code
 *  {"$or": [
 *     {"age": {"$gte": 30}},
 *     {"status": {"$eq": "active"}}
 *  ]}
 * }</pre></blockquote>
 *
 * @author Hamza Assada 23/03/2025 (7amza.it@gmail.com)
 * @see JpaQueryBuilder
 */
public class UnifiedQueryParser {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Parses a JSON string representing a query expression.
     */
    public static FilterExpression parse(String json) throws Exception {
        Map<String, Object> rawQuery = objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {
        });
        return parseExpression(rawQuery);
    }

    private static FilterExpression parseExpression(Object node) {
        if (node instanceof Map<?, ?> map) {
            // Check for logical operators ($and, $or)
            if (map.containsKey("$and") || map.containsKey("$or")) {
                LogicalOperator op = map.containsKey("$and") ? LogicalOperator.AND : LogicalOperator.OR;
                List<?> rawList = (List<?>) (op == LogicalOperator.AND ? map.get("$and") : map.get("$or"));
                List<FilterExpression> expressions = rawList.stream()
                    .map(UnifiedQueryParser::parseExpression)
                    .collect(Collectors.toList());
                return new CompoundFilter(op, expressions);
            } else {
                List<FilterExpression> expressions = new ArrayList<>();
                for (Map.Entry<?, ?> entry : map.entrySet()) {
                    String field = entry.getKey().toString();
                    Object value = entry.getValue();
                    if (value instanceof Map<?, ?> opMap) {
                        for (Map.Entry<?, ?> opEntry : opMap.entrySet()) {
                            String opKey = opEntry.getKey().toString();
                            Object opValue = opEntry.getValue();
                            FilterOperator operator = FilterOperator.fromOperatorStr(opKey)
                                .orElseThrow(() -> new IllegalArgumentException("Unknown operator: " + opKey));
                            expressions.add(new SimpleFilter(field, operator, opValue));
                        }
                    } else {
                        // If value is not a map, assume equality
                        expressions.add(new SimpleFilter(field, FilterOperator.EQ, value));
                    }
                }
                return expressions.size() == 1 ? expressions.get(0)
                    : new CompoundFilter(LogicalOperator.AND, expressions);
            }
        }
        throw new IllegalArgumentException("Invalid query expression node: " + node);
    }

    public static void main(String[] args) {
        String jsonQuery = """
                {
                  "$or": [
                    {"name": {"$eq": "ANC"}},
                    {"age": {"$gte": 30}}
                  ]
                }
            """;

        try {
            FilterExpression expression = UnifiedQueryParser.parse(jsonQuery);
            System.out.println(expression);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}

