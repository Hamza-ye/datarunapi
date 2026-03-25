package org.nmcpye.datarun.assignment.activity.repository;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;

import org.nmcpye.datarun.assignment.activity.Activity;
import org.nmcpye.datarun.assignment.assignment.Assignment;
import org.nmcpye.datarun.iam.team.Team;
import org.nmcpye.datarun.iam.user.User;
import org.nmcpye.datarun.iam.security.SecurityUtils;
import org.nmcpye.datarun.sharedkernal.feedback.ErrorCode;
import org.nmcpye.datarun.sharedkernal.feedback.ErrorMessage;
import org.springframework.data.jpa.domain.Specification;

/**
 * @author Hamza Assada 11/02/2022
 */
public abstract class ActivitySpecifications {

    public static Specification<Activity> canRead() {
        return (root, query, criteriaBuilder) -> {
            Join<Activity, Assignment> assignmentJoin = root.join("assignments", JoinType.INNER);
            Join<Assignment, Team> teamJoin = assignmentJoin.join("team", JoinType.INNER);
            Join<Team, User> userJoin = teamJoin.join("users", JoinType.INNER);

            String currentUserLogin = SecurityUtils.getCurrentUserLoginOrThrow(
                new ErrorMessage(ErrorCode.E3004, Activity.class.getName()));

            return criteriaBuilder.equal(userJoin.get("login"), currentUserLogin);
        };
    }

    public static Specification<Activity> isEnabled() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.isFalse(root.get("disabled"));
    }
}

