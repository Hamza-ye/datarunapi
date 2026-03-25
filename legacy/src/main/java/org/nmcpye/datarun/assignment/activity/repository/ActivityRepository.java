package org.nmcpye.datarun.assignment.activity.repository;

import org.nmcpye.datarun.assignment.activity.Activity;
import org.nmcpye.datarun.sharedkernal.JpaIdentifiableRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Activity entity.
 *
 * @author Hamza Assada 11/02/2022
 */
@Repository
public interface ActivityRepository
    extends JpaIdentifiableRepository<Activity> {
    @Query(
        value = "select activity from Activity activity " +
            "join Assignment assignment on assignment.activity = activity " +
            "join Team team on assignment.team = team " +
            "join assignment.team.users u " +
            "where u.login = ?#{authentication.name} and activity.disabled = false",
        countQuery = "select count(activity) from Activity activity " +
            "join Assignment assignment on assignment.activity = activity " +
            "join Team team on assignment.team = team " +
            "join assignment.team.users u " +
            "where u.login = ?#{authentication.name} and activity.disabled = false"
    )
    Page<Activity> findAllByUser(Pageable pageable);
}
