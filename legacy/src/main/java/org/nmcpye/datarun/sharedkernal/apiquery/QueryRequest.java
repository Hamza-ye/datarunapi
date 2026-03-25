package org.nmcpye.datarun.sharedkernal.apiquery;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.nmcpye.datarun.sharedkernal.exceptions.IllegalQueryException;
import org.nmcpye.datarun.sharedkernal.feedback.ErrorCode;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@Setter
@Accessors(chain = true)
public class QueryRequest {

    @Schema(description = "Filters in format field__operator=value, e.g., status__eq=completed. " +
        "Supported operators: " +
        "eq (equal), " +
        "ne (not equal), " +
        "regex (regular expression), " +
        "in (in list), " +
        "exists (field existence), " +
        "gt (greater than), " +
        "gte (greater than or equal to), " +
        "lt (less than or equal to)")
    private Map<String, Object> filters = new HashMap<>();

    @Schema(description = "Comma-separated list of fields to include, e.g., id,uid,status")
    private String fields;

    @Schema(description = "Flag to enable fetching paged or not paged content")
    private boolean paged = true;

    @Schema(description = "Page number (default: 0)")
    private int page = 0;

    @Schema(description = "Page size (default: 20)")
    private int size = 20;

    @Schema(description = "Sort fields in format field,direction, e.g., finishedEntryTime,asc")
    private String sort;

    @Schema(description = "Flag to enable flattening of nested objects")
    private boolean flatten = false;

    @Schema(description = "Flag to enable advanced aggregation queries")
    private boolean aggregate = false;

    @Schema(description = "Flag to include soft deleted submissions")
    private boolean includeDeleted = false;

    @Schema(description = "Flag to include soft deleted submissions")
    private boolean includeDisabled = false;

    @Schema(description = "Flag to enable eagerLoad of nested objects")
    private boolean eagerload = false;

    @Schema(description = "Flag to merge DataFormTemplate elements and sections")
    private boolean mergeElements = false;

    @Schema(description = "Flag to only include elements update since a point of time")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Instant since;

    public boolean isEmpty() {
        return fields == null || fields.isEmpty();
    }

    public Map<String, Object> getParsedFilter() {
        return filters.entrySet().stream().collect(Collectors.toMap(
            Map.Entry::getKey,
            entry -> parseValue(entry.getKey(), entry.getValue())
        ));
    }

    ///
    public static Map<String, Object> parseIncomingFilter(String filter) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(filter, new TypeReference<Map<String, Object>>() {
        });
    }

    public Instant getSince() {
        return since != null ? since : Instant.EPOCH;
    }

    public Pageable getPageable() {
        if (!isPaged()) {
            return Pageable.unpaged();
        }

        return PageRequest.of(getPage(), getSize());
    }

    public int getSize() {
        if (!isPaged()) {
            return Integer.MAX_VALUE;
        }
        return size;
    }

    public int getPage() {
        if (!isPaged()) {
            return 0;
        }
        return page;
    }

    private Object parseValue(String key, Object value) {
        if (value instanceof String strValue) {
            try {
                if (key.endsWith("__eq") || key.endsWith("__ne")) {
                    return parseTypedValue(strValue);
                } else if (key.endsWith("__gt") || key.endsWith("__gte") ||
                    key.endsWith("__lt") || key.endsWith("__lte")) {
                    return parseNumber(strValue);
                } else if (key.endsWith("__exists")) {
                    return Boolean.parseBoolean(strValue);
                } else if (key.endsWith("__in")) {
                    return strValue.split(",");
                } else if (key.endsWith("__regex")) {
                    return strValue;
                }
            } catch (Exception e) {
                throw new IllegalQueryException(ErrorCode.E2014, "Failed to parse filter value for key: " + key + " " + e);
            }
        }
        return value;
    }

    private Object parseTypedValue(String value) {
        try {
            if (value.matches("-?\\d+")) {
                return Integer.parseInt(value);
            } else if (value.matches("-?\\d+\\.\\d+")) {
                return Double.parseDouble(value);
            } else if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
                return Boolean.parseBoolean(value);
            } else {
                // Default to String
                return value;
            }
        } catch (NumberFormatException e) {
            throw new IllegalQueryException(ErrorCode.E2014, "Invalid value format: " + value);
        }
    }

    private Number parseNumber(String value) {
        try {
            if (value.contains(".")) {
                return Double.parseDouble(value);
            } else {
                return Long.parseLong(value);
            }
        } catch (NumberFormatException e) {
            throw new IllegalQueryException(ErrorCode.E2014, "Invalid numeric value: " + value);
        }
    }

}
