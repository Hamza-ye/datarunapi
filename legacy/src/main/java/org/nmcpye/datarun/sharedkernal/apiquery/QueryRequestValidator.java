package org.nmcpye.datarun.sharedkernal.apiquery;

import java.util.List;

import org.nmcpye.datarun.sharedkernal.feedback.ErrorCode;
import org.nmcpye.datarun.sharedkernal.feedback.ErrorMessage;

@Deprecated(since = "api v1")
public class QueryRequestValidator {

    private static final List<String> SUPPORTED_OPERATORS = List.of(
        "eq", "ne", "regex", "in", "exists", "gt", "gte", "lt", "lte"
    );

    public static void validate(QueryRequest request) {
        request.getFilters().forEach((key, value) -> {
            String[] parts = key.split("__");
            if (parts.length == 0) {
                throw new QueryRequestValidationException(new ErrorMessage(ErrorCode.E2016));
            }

            String field = parts[0];
            String operator = parts.length > 1 ? parts[1] : "eq";

            if (!field.matches("^[a-zA-Z0-9._]+$")) {
                throw new QueryRequestValidationException(
                    "Invalid field name format: " + key + " " + operator);
            }

            if (!SUPPORTED_OPERATORS.contains(operator)) {
                throw new QueryRequestValidationException(new ErrorMessage(ErrorCode.E2035, operator));
            }

            validateValue(operator, value, key);
        });

        if (request.getSort() != null && !request.getSort().matches("^[a-zA-Z0-9._]+,(asc|desc)$")) {
            throw new QueryRequestValidationException(new ErrorMessage(ErrorCode.E2015, "Invalid sort format"));
        }

        if (request.getFields() != null && !request.getFields().matches("^[a-zA-Z0-9.,_]+$")) {
            throw new QueryRequestValidationException(new ErrorMessage(ErrorCode.E2016));
        }
    }

    private static void validateValue(String operator, Object value, String key) {
        switch (operator) {
            case "exists":
                if (!(value instanceof Boolean)) {
                    throw new QueryRequestValidationException(new ErrorMessage(ErrorCode.E2018, "exists", "true or false"));
                }
                break;
            case "in":
                if (!(value instanceof List)) {
                    throw new QueryRequestValidationException(new ErrorMessage(ErrorCode.E2018, "in", "list"));
                }
                break;
        }
    }
}
