package org.nmcpye.datarun.sharedkernal.apiquery.filter;

import lombok.Getter;
import lombok.Value;

import java.util.List;

/**
 * @author Hamza Assada 23/03/2025 (7amza.it@gmail.com)
 */
@Value
@Getter
public class CompoundFilter implements FilterExpression {
    LogicalOperator logicalOperator; // AND / OR
    List<FilterExpression> expressions;
}
