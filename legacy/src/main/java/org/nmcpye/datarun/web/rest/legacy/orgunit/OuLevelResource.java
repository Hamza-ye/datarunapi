package org.nmcpye.datarun.web.rest.legacy.orgunit;

import org.nmcpye.datarun.jpa.orgunit.OuLevel;
import org.nmcpye.datarun.jpa.orgunit.repository.OuLevelRepository;
import org.nmcpye.datarun.jpa.orgunit.service.OuLevelService;
import org.nmcpye.datarun.iam.security.AuthoritiesConstants;
import org.nmcpye.datarun.web.common.ApiVersion;
import org.nmcpye.datarun.web.rest.legacy.JpaBaseResource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.nmcpye.datarun.web.rest.legacy.orgunit.OuLevelResource.CUSTOM;
import static org.nmcpye.datarun.web.rest.legacy.orgunit.OuLevelResource.V1;

/**
 * REST controller for managing {@link OuLevel}.
 */
@RestController
@RequestMapping(value = {CUSTOM, V1})
@PreAuthorize("hasAnyAuthority(\"" + AuthoritiesConstants.ADMIN + "\", \"" + AuthoritiesConstants.USER + "\")")
public class OuLevelResource
    extends JpaBaseResource<OuLevel> {
    protected static final String NAME = "/ouLevels";
    protected static final String CUSTOM = ApiVersion.API_CUSTOM + NAME;
    protected static final String V1 = ApiVersion.API_V1 + NAME;

    private final OuLevelService ouLevelService;

    private final OuLevelRepository ouLevelRepositoryCustom;

    public OuLevelResource(OuLevelService ouLevelService,
                           OuLevelRepository ouLevelRepository) {
        super(ouLevelService, ouLevelRepository);
        this.ouLevelService = ouLevelService;
        this.ouLevelRepositoryCustom = ouLevelRepository;
    }

    @Override
    protected String getName() {
        return "ouLevels";
    }
}
