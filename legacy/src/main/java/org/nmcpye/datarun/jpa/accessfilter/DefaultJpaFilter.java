package org.nmcpye.datarun.jpa.accessfilter;

import org.nmcpye.datarun.iam.security.CurrentUserDetails;
import org.nmcpye.datarun.sharedkernal.IdentifiableObject;
import org.nmcpye.datarun.sharedkernal.apiquery.QueryRequest;
import org.springframework.data.jpa.domain.Specification;

import java.lang.reflect.ParameterizedType;

/**
 * @author Hamza Assada
 * @since 21/03/2025
 */
@SuppressWarnings("unchecked")
public class DefaultJpaFilter<T extends IdentifiableObject<?>>
    implements AccessFilter<T> {
    private final Class<T> klass;

    public DefaultJpaFilter() {
        this.klass = (Class<T>) ((ParameterizedType) getClass()
            .getGenericSuperclass())
            .getActualTypeArguments()[0];
    }

    @Override
    public Class<T> getKlass() {
        return klass;
    }

    @Override
    public Specification<T> getAccessSpecification(CurrentUserDetails user,
                                                   QueryRequest queryRequest) {
        return AccessFilter.createDefaultSpecification(user);
    }
}
