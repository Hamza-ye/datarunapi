package org.nmcpye.datarun.iam.team.service;

import org.nmcpye.datarun.iam.team.Team;
import org.nmcpye.datarun.sharedkernal.JpaIdentifiableObjectService;
import org.nmcpye.datarun.sharedkernal.apiquery.QueryRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * Service Interface for managing {@link Team}.
 */
public interface TeamService
    extends JpaIdentifiableObjectService<Team> {

    Page<Team> findAllManagedByUser(Pageable pageable, QueryRequest queryRequest);

    Optional<Team> partialUpdate(Team team);
}
