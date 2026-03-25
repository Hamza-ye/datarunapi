package org.nmcpye.datarun.web.rest.legacy.orgunit;

import org.nmcpye.datarun.jpa.orgunit.OrgUnitGroup;
import org.nmcpye.datarun.jpa.orgunit.repository.OrgUnitGroupRepository;
import org.nmcpye.datarun.jpa.orgunit.service.OrgUnitGroupService;
import org.nmcpye.datarun.iam.security.AuthoritiesConstants;
import org.nmcpye.datarun.web.common.ApiVersion;
import org.nmcpye.datarun.web.rest.legacy.JpaBaseResource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.nmcpye.datarun.web.rest.legacy.orgunit.OrgUnitGroupResource.CUSTOM;
import static org.nmcpye.datarun.web.rest.legacy.orgunit.OrgUnitGroupResource.V1;

/**
 * REST Extended controller for managing {@link OrgUnitGroup}.
 */
@RestController
@RequestMapping(value = {
    CUSTOM,
    V1})
@PreAuthorize("hasAnyAuthority(\"" + AuthoritiesConstants.ADMIN + "\", \"" + AuthoritiesConstants.USER + "\")")
public class OrgUnitGroupResource extends JpaBaseResource<OrgUnitGroup> {
    protected static final String NAME = "/orgUnitGroups";
    protected static final String CUSTOM = ApiVersion.API_CUSTOM + NAME;
    protected static final String V1 = ApiVersion.API_V1 + NAME;

    public OrgUnitGroupResource(OrgUnitGroupService service, OrgUnitGroupRepository repository) {
        super(service, repository);
    }

    @Override
    protected String getName() {
        return "orgUnitGroups";
    }

//
//    @Override
//    @PreAuthorize("hasAnyAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
//    public ResponseEntity<OrgUnitGroup> updateEntity(String uid, OrgUnitGroup entity) throws URISyntaxException {
//        return super.updateEntity(uid, entity);
//    }
//
//    @PreAuthorize("hasAnyAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
//    @Override
//    public ResponseEntity<?> saveReturnSaved(OrgUnitGroup entity) {
//        return super.saveReturnSaved(entity);
//    }
//
//    @PreAuthorize("hasAnyAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
//    @Override
//    public ResponseEntity<EntitySaveSummaryVM> saveOne(OrgUnitGroup entity) {
//        return super.saveOne(entity);
//    }
//
//    @PreAuthorize("hasAnyAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
//    @Override
//    public ResponseEntity<EntitySaveSummaryVM> saveAll(List<OrgUnitGroup> entities) {
//        return super.saveAll(entities);
//    }
}
