package org.nmcpye.datarun.party.repository;

import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class AssignmentRoleDataPolicyJooqRepository {

    private final DSLContext dsl;

    /**
     * Returns data_template.uids allowed for the given user/principals in an
     * assignment.
     *
     * @param assignmentId assignment.id (string)
     * @param userId       current user id
     * @param principalIds set of principal ids (team ids, usergroup ids, etc). May
     *                     be empty.
     * @param userRoles    set of roles from assignment_member for this
     *                     user/principals. May be empty.
     */
    public List<String> findAllowedTemplateUids(String assignmentId,
            String userId,
            Set<String> principalIds,
            Set<String> userRoles) {

        var ADT = DSL.table(DSL.name("assignment_data_template"));
        var DT = DSL.table(DSL.name("data_template"));

        var ADT_assignment = DSL.field(DSL.name("assignment_data_template", "assignment_id"), String.class);
        var ADT_dtId = DSL.field(DSL.name("assignment_data_template", "data_template_id"), String.class);
        var ADT_principalId = DSL.field(DSL.name("assignment_data_template", "principal_id"), String.class);
        var ADT_principalRole = DSL.field(DSL.name("assignment_data_template", "principal_role"), String.class);
        var ADT_principalType = DSL.field(DSL.name("assignment_data_template", "principal_type"), String.class);

        var DT_id = DSL.field(DSL.name("data_template", "id"), String.class);
        var DT_uid = DSL.field(DSL.name("data_template", "uid"), String.class);

        var condPrincipalExact = ADT_principalId.eq(userId);
        var condPrincipalSet = (principalIds == null || principalIds.isEmpty())
                ? DSL.falseCondition()
                : ADT_principalId.in(principalIds);
        var condRole = (userRoles == null || userRoles.isEmpty())
                ? DSL.falseCondition()
                : ADT_principalRole.in(userRoles);

        var condGlobal = ADT_principalId.isNull()
                .and(ADT_principalRole.isNull())
                .and(ADT_principalType.isNull());

        var finalCond = ADT_assignment.eq(assignmentId)
                .and(condPrincipalExact.or(condPrincipalSet).or(condRole).or(condGlobal));

        List<Record1<String>> recs = dsl.selectDistinct(DT_uid)
                .from(ADT)
                .join(DT).on(DT_id.eq(ADT_dtId))
                .where(finalCond)
                .fetch();

        if (recs.isEmpty())
            return Collections.emptyList();

        return recs.stream().map(Record1::value1).collect(Collectors.toList());
    }
}
