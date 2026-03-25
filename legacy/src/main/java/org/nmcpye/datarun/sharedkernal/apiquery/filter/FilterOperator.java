package org.nmcpye.datarun.sharedkernal.apiquery.filter;

import java.util.Optional;

/**
 * @author Hamza Assada 23/03/2025 (7amza.it@gmail.com)
 */
public enum FilterOperator {
    EQ, NE, GT, GTE, LT, LTE, IN, EXISTS, REGEX, NULL;

    public static Optional<FilterOperator> fromOperatorStr(String op) {
        return switch (op) {
            case "$eq" -> Optional.of(EQ);
            case "$ne" -> Optional.of(NE);
            case "$gt" -> Optional.of(GT);
            case "$gte" -> Optional.of(GTE);
            case "$lt" -> Optional.of(LT);
            case "$lte" -> Optional.of(LTE);
            case "$in" -> Optional.of(IN);
            case "$exists" -> Optional.of(EXISTS);
            case "$regex" -> Optional.of(REGEX);
            default -> Optional.empty();
        };
    }
}
