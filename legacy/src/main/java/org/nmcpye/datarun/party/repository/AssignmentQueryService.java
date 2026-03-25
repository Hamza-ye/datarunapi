package org.nmcpye.datarun.party.repository;

import lombok.RequiredArgsConstructor;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;
import java.util.Set;

/**
 * Service to find active assignment ids for principals/teams with optional
 * filters.
 * Uses jOOQ DSL with plain table/field names (no codegen).
 */
@Service
@RequiredArgsConstructor
@SuppressWarnings("DuplicatedCode")
public class AssignmentQueryService {

    private final DSLContext dsl;

    /**
     * Filters holder (nullable fields ignored).
     */
    public static class AssignmentFilters {
        public String teamId;
        public String teamUid;
        public String orgUnitId;
        public String orgUnitUid;
        public String activityId;
        public String activityUid;
        public Boolean onlyActiveStatus; // optional: e.g. assignment.status = 'ACTIVE'
    }

    /**
     * Main pageable method. If pageable.isUnpaged(), it delegates to list variant
     * to avoid COUNT.
     */
    public Page<String> findActiveAssignmentIds(Set<String> principalKeys,
            Set<String> teamIds,
            AssignmentFilters filters,
            Pageable pageable) {

        if (pageable == null)
            pageable = Pageable.unpaged();
        if (pageable.isUnpaged()) {
            List<String> list = findActiveAssignmentIdsList(principalKeys, teamIds, filters);
            return new PageImpl<>(list, Pageable.unpaged(), list.size());
        }

        // build base select
        Table<?> a = DSL.table(DSL.name("assignment"));
        Field<String> aId = DSL.field(DSL.name("assignment", "id"), String.class);

        // Build condition
        Condition cond = buildAssignmentCondition(principalKeys, teamIds, filters);

        // build select distinct
        SelectJoinStep<Record1<String>> base = dsl.selectDistinct(aId).from(a);
        // apply filters by joining team/activity/org_unit if needed for uid filters
        base = applyJoinsIfNeeded(base, filters);

        SelectConditionStep<Record1<String>> sel = base.where(cond);

        // count
        long total = dsl.fetchCount(sel);

        // fetch page
        int pageSize = pageable.getPageSize();
        long offset = pageable.getOffset();
        List<String> ids = dsl.selectDistinct(aId)
                .from(a)
                .where(cond)
                .limit(pageSize)
                .offset((int) offset)
                .fetch(aId);

        return new PageImpl<>(ids, pageable, total);
    }

    /**
     * List variant (no COUNT) used when pageable.unpaged()
     */
    public List<String> findActiveAssignmentIdsList(Set<String> principalKeys,
            Set<String> teamIds,
            AssignmentFilters filters) {

        Table<?> a = DSL.table(DSL.name("assignment"));
        Field<String> aId = DSL.field(DSL.name("assignment", "id"), String.class);

        Condition cond = buildAssignmentCondition(principalKeys, teamIds, filters);

        // apply joins if filters by uid are used
        SelectJoinStep<Record1<String>> base = dsl.selectDistinct(aId).from(a);
        base = applyJoinsIfNeeded(base, filters);

        return base.where(cond).fetch(aId);
    }

    /*
     * -------------------------
     * Helper builders
     * -------------------------
     */

    /**
     * Build the main WHERE condition:
     * EXISTS (assignment_member matching principalKeys)
     * OR (NOT EXISTS (assignment_member) AND assignment.team_id IN teamIds AND
     * team/activity enabled & valid)
     */
    private Condition buildAssignmentCondition(Set<String> principalKeys,
            Set<String> teamIds,
            AssignmentFilters filters) {

        Table<?> am = DSL.table(DSL.name("assignment_member"));
        Field<String> amAssignmentId = DSL.field(DSL.name("assignment_member", "assignment_id"), String.class);
        Field<String> amMemberType = DSL.field(DSL.name("assignment_member", "member_type"), String.class);
        Field<String> amMemberId = DSL.field(DSL.name("assignment_member", "member_id"), String.class);
        Field<Object> amValidFrom = DSL.field(DSL.name("assignment_member", "valid_from"));
        Field<Object> amValidTo = DSL.field(DSL.name("assignment_member", "valid_to"));

        Field<Timestamp> now = DSL.currentTimestamp();

        // principal condition: (member_type || ':' || member_id) IN (:principalKeys)
        Condition principalMatch = DSL.trueCondition();
        if (principalKeys == null || principalKeys.isEmpty()) {
            // impossible condition if no principals provided
            principalMatch = DSL.falseCondition();
        } else {
            Field<String> principalExpr = amMemberType.concat(DSL.inline(":")).concat(amMemberId);
            principalMatch = principalExpr.in(principalKeys);
        }

        // optional member validity checks (kept here but can be removed if not desired)
        Condition memberValidity = amValidFrom.isNull().or(amValidFrom.le(now))
                .and(amValidTo.isNull().or(amValidTo.gt(now)));

        // exists subquery
        Condition existsMembers = DSL.exists(
                DSL.selectOne()
                        .from(am)
                        .where(amAssignmentId.eq(DSL.field(DSL.name("assignment", "id"), String.class)))
                        .and(principalMatch)
                        .and(memberValidity));

        // second branch: no members exist and fallback to assignment.team_id in teamIds
        // and team/activity enabled
        Table<?> t = DSL.table(DSL.name("team"));
        Table<?> act = DSL.table(DSL.name("activity"));

        Field<String> aTeamId = DSL.field(DSL.name("assignment", "team_id"), String.class);
        Field<String> aActivityId = DSL.field(DSL.name("assignment", "activity_id"), String.class);

        Condition noMembers = DSL.notExists(
                DSL.selectOne().from(am)
                        .where(amAssignmentId.eq(DSL.field(DSL.name("assignment", "id"), String.class))));

        Condition teamIn = (teamIds == null || teamIds.isEmpty())
                ? DSL.falseCondition()
                : aTeamId.in(teamIds);

        Field<Boolean> tDisabled = DSL.field(DSL.name("team", "disabled"), Boolean.class);
        Field<Boolean> actDisabled = DSL.field(DSL.name("activity", "disabled"), Boolean.class);
        Field<Object> actValidFrom = DSL.field(DSL.name("activity", "valid_from"));
        Field<Object> actValidTo = DSL.field(DSL.name("activity", "valid_to"));

        Condition teamEnabled = tDisabled.isNull().or(tDisabled.eq(false));
        Condition activityEnabled = actDisabled.isNull().or(actDisabled.eq(false));
        Condition activityValid = actValidFrom.isNull().or(actValidFrom.le(DSL.currentTimestamp()))
                .and(actValidTo.isNull().or(actValidTo.gt(DSL.currentTimestamp())));

        Condition fallback = noMembers.and(teamIn).and(teamEnabled).and(activityEnabled).and(activityValid);

        // combine
        Condition combined = existsMembers.or(fallback);

        // apply optional extra filters (team.uid, activity.uid, org_unit filters)
        combined = applyOptionalFilters(combined, filters);

        return combined;
    }

    /**
     * Apply optional filters that require joining other tables (team.uid,
     * activity.uid, org_unit.uid)
     */
    private Condition applyOptionalFilters(Condition base, AssignmentFilters filters) {
        if (filters == null)
            return base;
        Condition extra = DSL.trueCondition();

        if (filters.teamUid != null) {
            // need join to team: assignment.team_id -> team.id and team.uid = :teamUid
            extra = extra.and(DSL.field(DSL.name("team", "uid"), String.class).eq(filters.teamUid));
        }
        if (filters.activityUid != null) {
            extra = extra.and(DSL.field(DSL.name("activity", "uid"), String.class).eq(filters.activityUid));
        }
        if (filters.orgUnitUid != null) {
            extra = extra.and(DSL.field(DSL.name("org_unit", "uid"), String.class).eq(filters.orgUnitUid));
        }
        return base.and(extra);
    }

    /**
     * Adds joins in the SELECT if filters by UID are present (so those fields exist
     * in FROM).
     */
    private SelectJoinStep<Record1<String>> applyJoinsIfNeeded(SelectJoinStep<Record1<String>> select,
            AssignmentFilters filters) {
        Table<?> a = DSL.table(DSL.name("assignment"));
        if (filters == null)
            return select;
        if (filters.teamUid != null) {
            select = select.leftJoin(DSL.table(DSL.name("team")))
                    .on(DSL.field(DSL.name("team", "id")).eq(DSL.field(DSL.name("assignment", "team_id"))));
        }
        if (filters.activityUid != null) {
            select = select.leftJoin(DSL.table(DSL.name("activity")))
                    .on(DSL.field(DSL.name("activity", "id")).eq(DSL.field(DSL.name("assignment", "activity_id"))));
        }
        if (filters.orgUnitUid != null) {
            select = select.leftJoin(DSL.table(DSL.name("org_unit")))
                    .on(DSL.field(DSL.name("org_unit", "id")).eq(DSL.field(DSL.name("assignment", "org_unit_id"))));
        }
        return select;
    }
}
