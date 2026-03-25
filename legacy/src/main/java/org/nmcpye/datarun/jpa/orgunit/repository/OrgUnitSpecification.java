package org.nmcpye.datarun.jpa.orgunit.repository;

import jakarta.persistence.criteria.*;

import org.nmcpye.datarun.assignment.activity.Activity;
import org.nmcpye.datarun.assignment.assignment.Assignment;
import org.nmcpye.datarun.jpa.orgunit.OrgUnit;
import org.nmcpye.datarun.iam.team.Team;
import org.nmcpye.datarun.iam.user.User;
import org.nmcpye.datarun.iam.security.AuthoritiesConstants;
import org.nmcpye.datarun.iam.security.SecurityUtils;
import org.nmcpye.datarun.sharedkernal.feedback.ErrorCode;
import org.nmcpye.datarun.sharedkernal.feedback.ErrorMessage;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

/**
 * @author Hamza Assada 05/06/2023
 */
public abstract class OrgUnitSpecification {
    public static Specification<OrgUnit> hasAncestors(
        List<Long> accessibleIds, boolean includeDisabled
    ) {
        return (root, query, cb) -> {
            // Subquery to get all ancestor UIDs from accessible OrgUnits' paths
            Subquery<String> subquery = query.subquery(String.class);
            Root<OrgUnit> acc = subquery.from(OrgUnit.class);
            subquery.select(
                cb.function("unnest", String.class,
                    cb.function("string_to_array", String[].class, acc.get("path"), cb.literal(","))
                )
            ).where(acc.get("id").in(accessibleIds));

            /// isAncestor
            // Match current OrgUnit's UID against the collected ancestor UIDs
            return root.get("uid").in(subquery);
        };
    }

    public static Specification<OrgUnit> hasDescendants(
        List<Long> accessibleIds, boolean includeDisabled
    ) {
        return (root, query, cb) -> {
            // Subquery to get paths of accessible OrgUnits
            Subquery<String> subquery = query.subquery(String.class);
            Root<OrgUnit> acc = subquery.from(OrgUnit.class);
            subquery.select(acc.get("path"))
                .where(acc.get("id").in(accessibleIds));

            // Check if current OrgUnit's path starts with any accessible path

            return cb.or(
                root.get("id").in(accessibleIds), // Include the accessible OrgUnit itself
                cb.exists(subquery.where(
                    cb.like(root.get("path"), cb.concat(acc.get("path"), ",%"))
                ))
            );
        };
    }

    // Usage: Combine with additional filters
//    List<OrgUnit> results = repo.findAll(
//        hasAncestors(accessibleIds)
//            .and((root, query, cb) -> cb.like(root.get("name"), "%HQ%"))
//    );

    public static Specification<OrgUnit> hasLevel(Integer level) {
        return (root, query, criteriaBuilder) -> level == null ? null : criteriaBuilder.equal(root.get("level"), level);
    }

    public static Specification<OrgUnit> hasParent(String parent) {
        return (root, query, criteriaBuilder) -> parent == null ? null : criteriaBuilder.equal(root.get("parent"), parent);
    }

    public static Specification<OrgUnit> selectedUnitsByUids(List<String> uids) {
        return (root, query, cb) -> root.get("uid").in(uids);
    }

    public static Specification<OrgUnit> canRead() {
        return (root, query, criteriaBuilder) -> {
            if (SecurityUtils.hasCurrentUserAnyOfAuthorities(AuthoritiesConstants.ADMIN)) {
                return criteriaBuilder.conjunction();
            } else if (!SecurityUtils.isAuthenticated()) {
                return null;
            } else {
                if (Long.class != query.getResultType()) {
                    root.fetch("parent", JoinType.LEFT);
                }
                Join<OrgUnit, Assignment> assignmentJoin = root.join("assignments", JoinType.INNER);
                Join<Assignment, Activity> assignmentActivityJoin = assignmentJoin.join("activity", JoinType.INNER);
                Join<Assignment, Team> teamJoin = assignmentJoin.join("team", JoinType.INNER);
                Join<Team, User> userJoin = teamJoin.join("users", JoinType.INNER);

                Predicate teamNotDisabled = criteriaBuilder.isFalse(teamJoin.get("disabled"));
                Predicate activityNotDisabled = criteriaBuilder.isFalse(assignmentActivityJoin.get("disabled"));

                String currentUserLogin = SecurityUtils.getCurrentUserLoginOrThrow(
                    new ErrorMessage(ErrorCode.E3004, OrgUnit.class.getName()));
                query.distinct(true);
                return criteriaBuilder.and(criteriaBuilder.equal(userJoin.get("login"), currentUserLogin),
                    activityNotDisabled,
                    teamNotDisabled);
            }
        };
    }

//    public Specification<OrgUnit> canRead(String login) {
//        return (root, query, criteriaBuilder) -> {
//            Join<OrgUnit, Assignment> assignmentsJoin = root.join("assignments", JoinType.INNER);
//            Join<Assignment, Team> teamJoin = assignmentsJoin.join("team", JoinType.INNER);
//            Join<Team, User> userJoin = teamJoin.join("users", JoinType.INNER);
//
//            return criteriaBuilder.equal(userJoin.get("login"), login);
//        };
//    }


//    public Specification<OrgUnit> canReadWithAncestors(String login) {
//        return (root, query, criteriaBuilder) -> {
//            // Subquery to fetch distinct paths of readable OrgUnits
//            Subquery<String> readableOrgUnitPaths = query.subquery(String.class);
//            Root<OrgUnit> subRoot = readableOrgUnitPaths.from(OrgUnit.class);
//
//            // Joins for assignments -> team -> users
//            Join<OrgUnit, Assignment> assignmentJoin = subRoot.join("assignments", JoinType.INNER);
//            Join<Assignment, Team> teamJoin = assignmentJoin.join("team", JoinType.INNER);
//            Join<Team, User> userJoin = teamJoin.join("users", JoinType.INNER);
//
//            // Fetch distinct paths of readable OrgUnits
//            readableOrgUnitPaths.select(subRoot.get("path"))
//                .where(criteriaBuilder.equal(userJoin.get("login"), login));
//
//            // Main query: match direct OrgUnits and their ancestors
//            return criteriaBuilder.or(
//                root.get("path").in(readableOrgUnitPaths), // Direct readable OrgUnits
//                criteriaBuilder.exists(
//                    query.subquery(String.class)
//                        .select(criteriaBuilder.literal(1)) // Check existence of ancestor relationship
//                        .where(
//                            criteriaBuilder.and(
//                                criteriaBuilder.like(root.get("path"), criteriaBuilder.concat(subRoot.get("path"), "%")),
//                                subRoot.get("path").in(readableOrgUnitPaths) // Ensure ancestor paths are unique
//                            )
//                        )
//                )
//            );
//        };
//    }


//    public Specification<OrgUnit> canReadWithAncestors(String login) {
//        return (root, query, criteriaBuilder) -> {
//            // Subquery to fetch orgUnit paths the user can read
//            Subquery<String> readableOrgUnitPaths = query.subquery(String.class);
//            Root<OrgUnit> subRoot = readableOrgUnitPaths.from(OrgUnit.class);
//
//            // Joins for assignments -> team -> users
//            Join<OrgUnit, Assignment> assignmentJoin = subRoot.join("assignments", JoinType.INNER);
//            Join<Assignment, Team> teamJoin = assignmentJoin.join("team", JoinType.INNER);
//            Join<Team, User> userJoin = teamJoin.join("users", JoinType.INNER);
//
//            // Fetch distinct paths of readable OrgUnits
//            readableOrgUnitPaths.select(subRoot.get("path")).distinct(true)
//                .where(criteriaBuilder.equal(userJoin.get("login"), login));
//
//            // Main query: include ancestors explicitly
//            return criteriaBuilder.or(
//                root.get("path").in(readableOrgUnitPaths), // OrgUnits readable by the user
//                criteriaBuilder.exists(
//                    query.subquery(String.class)
//                        .select(subRoot.get("path"))
//                        .where(criteriaBuilder.like(root.get("path"), subRoot.get("path") + "%"))
//                )
//            );
//        };
//    }

//    public Specification<OrgUnit> canRead(String login) {
//        return (root, query, criteriaBuilder) -> {
//            // Subquery to fetch orgUnit paths the user can read
//            Subquery<String> readableOrgUnitPaths = query.subquery(String.class);
//            Root<OrgUnit> subRoot = readableOrgUnitPaths.from(OrgUnit.class);
//
//            // Joins for assignments -> team -> users
//            Join<OrgUnit, Assignment> assignmentJoin = subRoot.join("assignments", JoinType.INNER);
//            Join<Assignment, Team> teamJoin = assignmentJoin.join("team", JoinType.INNER);
//            Join<Team, User> userJoin = teamJoin.join("users", JoinType.INNER);
//
//            // Fetch paths of readable OrgUnits
//            readableOrgUnitPaths.select(subRoot.get("path"))
//                .where(criteriaBuilder.equal(userJoin.get("login"), login));
//
//            // Main query: include ancestors
//            return criteriaBuilder.or(
//                root.get("path").in(readableOrgUnitPaths), // OrgUnits readable by the user
//                readableOrgUnitPaths.distinct(true).getSelection().in(root.get("path")) // Ancestors of readable OrgUnits
//            );
//        };
//    }


//    public Specification<OrgUnit> canReadWithAncestors(String login) {
//        return (root, query, criteriaBuilder) -> {
//            // Subquery for organizational units the user can read
//            Subquery<String> orgUnitsSubquery = query.subquery(String.class);
//            Root<OrgUnit> orgUnitsRoot = orgUnitsSubquery.from(OrgUnit.class);
//
//            // Joins for assignments, teams, and users
//            Join<OrgUnit, Assignment> assignmentsJoin = orgUnitsRoot.join("assignments", JoinType.INNER);
//            Join<Assignment, Team> teamJoin = assignmentsJoin.join("team", JoinType.INNER);
//            Join<Team, User> userJoin = teamJoin.join("users", JoinType.INNER);
//
//            // Subquery selection
//            orgUnitsSubquery.select(orgUnitsRoot.get("path"))
//                .where(criteriaBuilder.equal(userJoin.get("login"), login));
//
//            // Main query to include ancestors based on path
//            return criteriaBuilder.or(
//                criteriaBuilder.equal(userJoin.get("login"), login), // Direct match
//                criteriaBuilder.like(root.get("path"), criteriaBuilder.concat(orgUnitsSubquery, "%")) // Ancestors
//            );
//        };
//    }

//    public Specification<OrgUnit> canRead(String login) {
//        return (root, query, criteriaBuilder) -> {
////            Join<OrgUnit, Assignment> assignmentsJoin = root.join("assignments", JoinType.INNER);
////            Join<Assignment, Team> teamJoin = assignmentsJoin.join("team", JoinType.INNER);
////            Join<Team, User> userJoin = teamJoin.join("users", JoinType.INNER);
//
//            // Subquery to find OrgUnits directly assigned to the user
//            Subquery<String> subquery = query.subquery(String.class);
//            Root<OrgUnit> subRoot = subquery.from(OrgUnit.class);
//            Join<OrgUnit, Assignment> subAssignmentsJoin = subRoot.join("assignments", JoinType.INNER);
//            Join<Assignment, Team> subTeamJoin = subAssignmentsJoin.join("team", JoinType.INNER);
//            Join<Team, User> subUserJoin = subTeamJoin.join("users", JoinType.INNER);
//            subquery.select(subRoot.get("id")).where(criteriaBuilder.equal(subUserJoin.get("login"), login));
//
//            // Main query to include ancestors of the OrgUnits found in the subquery
//            return criteriaBuilder.or(
//                root.get("id").in(subquery),  // OrgUnits directly assigned to the user
//                criteriaBuilder.like(root.get("path"), criteriaBuilder.concat("%,", criteriaBuilder.concat(criteriaBuilder.literal(","), root.get("id"))))  // Ancestors
//            );
//        };
//    }

}
