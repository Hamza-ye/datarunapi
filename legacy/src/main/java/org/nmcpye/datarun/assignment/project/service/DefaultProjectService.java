package org.nmcpye.datarun.assignment.project.service;

import org.nmcpye.datarun.jpa.accessfilter.UserAccessService;
import org.nmcpye.datarun.assignment.project.Project;
import org.nmcpye.datarun.assignment.project.repository.ProjectRepository;
import org.nmcpye.datarun.sharedkernal.DefaultJpaIdentifiableService;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Hamza Assada
 */
@Service
@Primary
@Transactional
public class DefaultProjectService extends DefaultJpaIdentifiableService<Project> implements ProjectService {

    public DefaultProjectService(ProjectRepository repository, CacheManager cacheManager, UserAccessService userAccessService) {
        super(repository, cacheManager, userAccessService);
    }
}
