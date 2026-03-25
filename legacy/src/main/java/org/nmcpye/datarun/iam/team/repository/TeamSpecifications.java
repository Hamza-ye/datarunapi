package org.nmcpye.datarun.iam.team.repository;

import jakarta.persistence.criteria.*;

import org.nmcpye.datarun.assignment.activity.Activity;
import org.nmcpye.datarun.iam.team.Team;
import org.nmcpye.datarun.iam.user.User;
import org.nmcpye.datarun.iam.security.AuthoritiesConstants;
import org.nmcpye.datarun.iam.security.CurrentUserDetails;
import org.nmcpye.datarun.iam.security.SecurityUtils;
import org.nmcpye.datarun.sharedkernal.apiquery.QueryRequest;
import org.nmcpye.datarun.sharedkernal.feedback.ErrorCode;
import org.nmcpye.datarun.sharedkernal.feedback.ErrorMessage;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public abstract class TeamSpecifications {
    static public Specification<Team> getAssignedSpecification(CurrentUserDetails user,
                                                               QueryRequest queryRequest) {
        Specification<Team> spec = (root, query, cb) -> {
            if (user.isSuper()) {
                return cb.conjunction();
            }

            if (user.getUserTeamsUIDs() == null || user.getUserTeamsUIDs().isEmpty()) {
                return cb.disjunction(); // user has no access
            }

            return root.get("uid").in(user.getUserTeamsUIDs());
        };

        if (queryRequest == null || !queryRequest.isIncludeDisabled()) {
            spec = spec.and(isEnabled());
        }
        return spec;
    }

    static public Specification<Team> getManagedSpecification(CurrentUserDetails user,
                                                      QueryRequest queryRequest) {
        Specification<Team> spec = (root, query, cb) -> {
            if (user.isSuper()) {
                return cb.conjunction();
            }

            if (user.getManagedTeamsUIDs() == null || user.getManagedTeamsUIDs().isEmpty()) {
                return cb.disjunction(); // user has no access
            }

            return root.get("uid").in(user.getManagedTeamsUIDs());

        };

        if (queryRequest == null || !queryRequest.isIncludeDisabled()) {
            spec = spec.and(isEnabled());
        }
        return spec;
    }

    public static Specification<Team> canRead() {
        return (root, query, criteriaBuilder) -> {
            if (SecurityUtils.hasCurrentUserAnyOfAuthorities(AuthoritiesConstants.ADMIN)) {
                return criteriaBuilder.conjunction();
            } else if (!SecurityUtils.isAuthenticated()) {
                return criteriaBuilder.disjunction();
            } else {
                String currentUserLogin = SecurityUtils.getCurrentUserLoginOrThrow(
                    new ErrorMessage(ErrorCode.E3004, Team.class.getName()));
                Join<Team, User> userJoin = root.join("users", JoinType.INNER);
                return criteriaBuilder.equal(userJoin.get("login"), currentUserLogin);
            }
        };
    }

    // Specification to get teams managed by teams that a user is part of
    public static Specification<Team> getManagedTeamsByCurrentUserTeams() {
        return (root, query, cb) -> {
            if (SecurityUtils.hasCurrentUserAnyOfAuthorities(AuthoritiesConstants.ADMIN)) {
                return cb.conjunction();
            } else if (!SecurityUtils.isAuthenticated()) {
                return cb.disjunction();
            } else {
                String userLogin = SecurityUtils.getCurrentUserLoginOrThrow(
                    new ErrorMessage(ErrorCode.E3004, Team.class.getName()));
                Subquery<Team> userTeamsSubquery = query.subquery(Team.class);
                Root<Team> userTeamRoot = userTeamsSubquery.from(Team.class);
                Join<Team, User> userJoin = userTeamRoot.join("users", JoinType.INNER);

                userTeamsSubquery.select(userTeamRoot)
                    .where(cb.equal(userJoin.get("login"), userLogin));

                Join<Team, Team> managedByJoin = root.join("managedByTeams", JoinType.INNER);
                return managedByJoin.in(userTeamsSubquery);
            }
        };
    }

    // Specification to get teams managed by teams that a user is part of
    public static Specification<Team> getManagedTeamsByUserTeams(String userLogin) {
        return (root, query, cb) -> {
            Subquery<Team> userTeamsSubquery = query.subquery(Team.class);
            Root<Team> userTeamRoot = userTeamsSubquery.from(Team.class);
            Join<Team, User> userJoin = userTeamRoot.join("users", JoinType.INNER);

            userTeamsSubquery.select(userTeamRoot)
                .where(cb.equal(userJoin.get("login"), userLogin));

            Join<Team, Team> managedByJoin = root.join("managedByTeams", JoinType.INNER);
            return managedByJoin.in(userTeamsSubquery);
        };
    }

    public static Specification<Team> isEnabled() {
        return (root, query, criteriaBuilder) -> {
            Join<Team, Activity> activityJoin = root.join("activity", JoinType.LEFT);

            Predicate teamNotDisabled = criteriaBuilder.isFalse(root.get("disabled"));
            Predicate activityNotDisabled = criteriaBuilder.isFalse(activityJoin.get("disabled"));

            return criteriaBuilder.and(teamNotDisabled, activityNotDisabled);
        };
    }

    // Specification to get managed teams for specific teams
    public static Specification<Team> getManagedTeamsForTeams(List<Long> teamIds) {
        return (root, query, cb) -> root.get("id").in(teamIds);
    }

    // Specification to get teams and their managed teams for a specific user login
    public static Specification<Team> getTeamsAndManagedTeamsForUser(String userLogin) {
        return (root, query, cb) -> {
            Subquery<Team> managedTeamsSubquery = query.subquery(Team.class);
            Root<Team> managedTeamRoot = managedTeamsSubquery.from(Team.class);
            Join<Team, Team> managedByJoin = managedTeamRoot.join("managedByTeams", JoinType.INNER);
            Join<Team, User> userJoin = managedByJoin.join("users", JoinType.INNER);

            managedTeamsSubquery.select(managedTeamRoot)
                .where(cb.equal(userJoin.get("login"), userLogin));

            Join<Team, User> rootUserJoin = root.join("users", JoinType.LEFT);
            return cb.or(
                cb.equal(rootUserJoin.get("login"), userLogin),
                root.in(managedTeamsSubquery)
            );
        };
    }

    // Specification to get teams that are not managed by any other team
    public static Specification<Team> getTopLevelTeams() {
        return (root, query, cb) -> cb.isEmpty(root.get("managedByTeams"));
    }

    // Specification to get teams that manage at least one other team
    public static Specification<Team> getTeamsWithManagedTeams() {
        return (root, query, cb) -> cb.isNotEmpty(root.get("managedTeams"));
    }

    // Specification to get teams managed by a specific team
    public static Specification<Team> getManagedTeamsByTeam(Long teamId) {
        return (root, query, cb) -> {
            Join<Team, Team> managedByJoin = root.join("managedByTeams", JoinType.INNER);
            return cb.equal(managedByJoin.get("id"), teamId);
        };
    }


    // Specification to get managed teams for a specific user login
//    public static Specification<Team> getManagedTeamsForUser(String userLogin) {
//        return (root, query, cb) -> {
//            Join<Team, User> userJoin = root.join("users", JoinType.INNER);
//            Join<Team, Team> managedTeamsJoin = root.join("managedTeams", JoinType.INNER);
//            return cb.and(
//                cb.equal(userJoin.get("login"), userLogin),
//                cb.isNotEmpty(root.get("managedTeams"))
//            );
//        };
//    }

//    @Override
//    public Predicate toPredicate(Root<Team> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
//
//        if (criteria.getOperation().equalsIgnoreCase(">")) {
//            return builder.greaterThanOrEqualTo(
//                root.get(criteria.getKey()), criteria.getValue().toString());
//        } else if (criteria.getOperation().equalsIgnoreCase("<")) {
//            return builder.lessThanOrEqualTo(
//                root.get(criteria.getKey()), criteria.getValue().toString());
//        } else if (criteria.getOperation().equalsIgnoreCase(":")) {
//            if (root.get(criteria.getKey()).getJavaType() == String.class) {
//                return builder.like(
//                    root.get(criteria.getKey()), "%" + criteria.getValue() + "%");
//            } else {
//                return builder.equal(root.get(criteria.getKey()), criteria.getValue());
//            }
//        }
//        return null;
//    }
}

