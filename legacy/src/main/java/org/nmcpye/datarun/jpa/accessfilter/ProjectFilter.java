package org.nmcpye.datarun.jpa.accessfilter;

import org.nmcpye.datarun.assignment.project.Project;
import org.nmcpye.datarun.iam.security.CurrentUserDetails;
import org.nmcpye.datarun.sharedkernal.apiquery.QueryRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

/**
 * @author Hamza Assada
 * @since 21/03/2025
 */
@Component
public class ProjectFilter extends DefaultJpaFilter<Project> {
    @Override
    public Specification<Project> getAccessSpecification(CurrentUserDetails user,
                                                         QueryRequest queryRequest) {
        return (root, query, cb) -> cb.isFalse(root.get("disabled"));
    }
}
