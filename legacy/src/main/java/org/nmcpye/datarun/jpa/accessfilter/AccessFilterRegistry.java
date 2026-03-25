package org.nmcpye.datarun.jpa.accessfilter;

import org.nmcpye.datarun.iam.security.CurrentUserDetails;
import org.nmcpye.datarun.sharedkernal.IdentifiableObject;
import org.nmcpye.datarun.sharedkernal.apiquery.QueryRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central Access Filter Registry
 *
 * @author Hamza Assada
 * @since 21/03/2025
 */
@Component
public class AccessFilterRegistry {

    private final List<AccessFilter<? extends IdentifiableObject<?>>> filterBeans;
    private final Map<Class<? extends IdentifiableObject<?>>, AccessFilter<? extends IdentifiableObject<?>>> filters = new ConcurrentHashMap<>();

    public AccessFilterRegistry(List<AccessFilter<? extends IdentifiableObject<?>>> filterBeans) {
        this.filterBeans = filterBeans;
    }

    public void registerFilters() {
        if (!filters.isEmpty()) {
            return;
        }
        filterBeans.forEach(filter ->
            filters.put(filter.getKlass(), filter)
        );
    }

    private AccessFilter<?> getFilter(Class<?> klass) {
        registerFilters();
        return filters.get(klass);
    }

    @SuppressWarnings("unchecked")
    public <T extends IdentifiableObject<?>> Specification<T> getSpecification(
        Class<T> entityClass, CurrentUserDetails user, QueryRequest queryRequest) {
        Specification<T> spec = Specification.where(null); // Start with empty specification
        AccessFilter<T> filter = (AccessFilter<T>) getFilter(entityClass);
        return spec.and(filter != null ?
            filter.getAccessSpecification(user, queryRequest) :
            AccessFilter.createDefaultSpecification(user));
    }
}
