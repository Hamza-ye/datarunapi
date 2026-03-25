package org.nmcpye.datarun.template.datatemplateelement;

public enum AggregationType {
    SUM("sum", true),
    SUM_TRUE("sum_true", true),
    AVG("avg", true),
    LAST("last", true), // Sum org unit
    FIRST("first", true),
    COUNT("count", true),
    COUNT_DISTINCT("count_distinct", true),
    MIN("min", true),
    MAX("max", true),
    NONE("none", true), // Aggregatable for text only
    PERCENT_TRUE("'percent_true", true),
    CONCAT("'concat", true),
    DEFAULT("default", false);

    private final String value;

    private boolean aggregatable;

    AggregationType(String value) {
        this.value = value;
    }

    AggregationType(String value, boolean aggregatable) {
        this.value = value;
        this.aggregatable = aggregatable;
    }

    public String getValue() {
        return value;
    }

    public boolean isAggregatable() {
        return aggregatable;
    }

    public boolean isDefault() {
        return this == DEFAULT;
    }

    public static AggregationType fromValue(String value) {
        for (AggregationType type : AggregationType.values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }

        return null;
    }
}
