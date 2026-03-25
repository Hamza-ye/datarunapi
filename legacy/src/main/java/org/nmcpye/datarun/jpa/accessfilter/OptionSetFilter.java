package org.nmcpye.datarun.jpa.accessfilter;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.nmcpye.datarun.template.datatemplateelement.FieldTemplateElementDto;
import org.nmcpye.datarun.jpa.accessfilter.entity.UserExecutionContext;
import org.nmcpye.datarun.template.datatemplate.TemplateVersion;
import org.nmcpye.datarun.jpa.option.OptionSet;
import org.nmcpye.datarun.iam.security.CurrentUserDetails;
import org.nmcpye.datarun.sharedkernal.apiquery.QueryRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

/**
 * @author Hamza Assada
 * @since 21/03/2025
 */
@Component
public class OptionSetFilter extends DefaultJpaFilter<OptionSet> {

    @Override
    public Specification<OptionSet> getAccessSpecification(CurrentUserDetails user,
            QueryRequest queryRequest) {
        return (root, query, cb) -> {
            if (user.isSuper()) {
                return cb.conjunction();
            }

            if (query == null) {
                return cb.conjunction();
            }

            // Path B: An OptionSet is accessible if it is part of a Form (TemplateVersion)
            // that the user has access to via the CQRS UserExecutionContext.
            // OptionSet <- FormDataElementConf -> TemplateVersion -> UserExecutionContext

            Subquery<String> sqConf = query.subquery(String.class);
            Root<FieldTemplateElementDto> confRoot = sqConf.from(FieldTemplateElementDto.class);
            Join<FieldTemplateElementDto, TemplateVersion> templateJoin = confRoot.join("templateVersion", JoinType.INNER);

            Subquery<String> sqUec = sqConf.subquery(String.class);
            Root<UserExecutionContext> uec = sqUec.from(UserExecutionContext.class);
            sqUec.select(uec.get("entityUid")).where(
                    cb.equal(uec.get("userUid"), user.getUid()),
                    cb.equal(uec.get("entityType"), "DATA_TEMPLATE"));

            sqConf.select(confRoot.get("optionSet"))
                    .where(
                            cb.isNotNull(confRoot.get("optionSet")),
                            templateJoin.get("templateUid").in(sqUec));

            return root.get("uid").in(sqConf);
        };
    }
}
