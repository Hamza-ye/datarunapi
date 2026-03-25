package org.nmcpye.datarun.jpa.accessfilter;

import org.nmcpye.datarun.template.datatemplate.TemplateVersion;
import org.nmcpye.datarun.iam.security.CurrentUserDetails;
import org.nmcpye.datarun.sharedkernal.apiquery.QueryRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

/**
 * @author Hamza Assada
 * @since 21/03/2025
 */
@Component
public class FormTemplateVersionFilter extends DefaultJpaFilter<TemplateVersion> {
    @Override
    public Specification<TemplateVersion> getAccessSpecification(CurrentUserDetails user,
                                                              QueryRequest queryRequest) {
        Specification<TemplateVersion> spec = (root, query, cb) -> {
            if (user.isSuper()) {
                return cb.conjunction();
            }

            if (user.getUserFormsUIDs() == null || user.getUserTeamsUIDs().isEmpty()) {
                return cb.disjunction(); // user has no access
            }

            return root.get("templateUid").in(user.getUserFormsUIDs());

        };

        return spec;
    }
}
