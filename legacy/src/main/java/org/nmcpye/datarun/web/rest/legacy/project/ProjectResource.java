package org.nmcpye.datarun.web.rest.legacy.project;

import org.nmcpye.datarun.assignment.project.Project;
import org.nmcpye.datarun.assignment.project.repository.ProjectRepository;
import org.nmcpye.datarun.assignment.project.service.ProjectService;
import org.nmcpye.datarun.iam.security.AuthoritiesConstants;
import org.nmcpye.datarun.web.common.ApiVersion;
import org.nmcpye.datarun.web.rest.legacy.JpaBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.nmcpye.datarun.web.rest.legacy.project.ProjectResource.CUSTOM;

/**
 * Legacy REST controller for managing {@link Project}.
 * Admin/CUSTOM path only — the V1 mobile path is now served by
 * {@link org.nmcpye.datarun.web.rest.v1.project.ProjectResource}.
 */
@RestController("projectResourceLegacy")
@RequestMapping(value = { CUSTOM })
@PreAuthorize("hasAnyAuthority(\"" + AuthoritiesConstants.ADMIN + "\", \"" + AuthoritiesConstants.USER + "\")")
public class ProjectResource extends JpaBaseResource<Project> {
    protected static final String NAME = "/projects";
    protected static final String CUSTOM = ApiVersion.API_CUSTOM + NAME;

    private final Logger log = LoggerFactory.getLogger(ProjectResource.class);

    private final ProjectService projectService;

    private final ProjectRepository projectRepository;

    public ProjectResource(ProjectService projectService,
            ProjectRepository projectRepository) {
        super(projectService, projectRepository);
        this.projectService = projectService;
        this.projectRepository = projectRepository;
    }

    @Override
    protected String getName() {
        return "projects";
    }

    // @PreAuthorize("hasAnyAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    // @Override
    // public ResponseEntity<Project> updateEntity(String uid, Project entity)
    // throws URISyntaxException {
    // return super.updateEntity(uid, entity);
    // }
    //
    // @PreAuthorize("hasAnyAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    // @Override
    // public ResponseEntity<?> saveReturnSaved(Project entity) {
    // return super.saveReturnSaved(entity);
    // }
    //
    // @PreAuthorize("hasAnyAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    // @Override
    // public ResponseEntity<EntitySaveSummaryVM> saveOne(Project entity) {
    // return super.saveOne(entity);
    // }
    //
    // @PreAuthorize("hasAnyAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    // @Override
    // public ResponseEntity<EntitySaveSummaryVM> saveAll(List<Project> entities) {
    // return super.saveAll(entities);
    // }
}
