package org.nmcpye.datarun.sharedkernal.apiquery;

import java.util.List;

import org.nmcpye.datarun.sharedkernal.apiquery.filter.FilterExpression;

/**
 * @author Hamza Assada 23/03/2025 (7amza.it@gmail.com)
 */
public interface QueryBuilder<T> {
    T buildQuery(List<FilterExpression> expressions);
}
