package org.nmcpye.datarun.assignment.project.repository;

import org.nmcpye.datarun.assignment.project.Project;
import org.nmcpye.datarun.sharedkernal.JpaIdentifiableRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Project entity.
 *
 * @author Hamza Assada 11/02/2022
 */
@SuppressWarnings("unused")
@Repository
public interface ProjectRepository
    extends JpaIdentifiableRepository<Project> {
}
