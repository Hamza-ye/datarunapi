package org.nmcpye.datarun.sharedkernal.apiquery.filter;

import lombok.Getter;
import lombok.Value;

/**
 * @author Hamza Assada 23/03/2025 (7amza.it@gmail.com)
 */
@Value
@Getter
public class SimpleFilter implements FilterExpression {
    String field;
    FilterOperator operator;
    Object value;
}
