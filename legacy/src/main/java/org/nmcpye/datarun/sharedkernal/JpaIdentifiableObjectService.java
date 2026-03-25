package org.nmcpye.datarun.sharedkernal;

import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;

import org.nmcpye.datarun.sharedkernal.apiquery.QueryRequest;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Hamza Assada
 * @since 20/03/2025
 */
public interface JpaIdentifiableObjectService<T extends JpaIdentifiableObject>
    extends IdentifiableObjectService<T, String> {
//    @Deprecated(since = "v 6 use mongo like json query")
    static <E extends JpaIdentifiableObject> Specification<E> buildQuerySpecification(QueryRequest queryRequest) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            queryRequest.getFilters().forEach((key, value) -> {
                if (key.contains(".")) {
                    // Handle nested properties, for example: parent.id
                    String[] parts = key.split("\\.");
                    Path<Object> path = root.get(parts[0]);
                    for (int i = 1; i < parts.length; i++) {
                        path = path.get(parts[i]);
                    }
                    predicates.add(cb.equal(path, value));
                } else {
                    // Handle simple properties
                    predicates.add(cb.equal(root.get(key), value));
                }
            });

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
