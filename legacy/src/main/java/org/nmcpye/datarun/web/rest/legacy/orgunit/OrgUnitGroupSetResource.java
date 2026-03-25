package org.nmcpye.datarun.web.rest.legacy.orgunit;

import org.nmcpye.datarun.jpa.orgunit.OrgUnitGroupSet;
import org.nmcpye.datarun.jpa.orgunit.repository.OrgUnitGroupSetRepository;
import org.nmcpye.datarun.jpa.orgunit.service.OrgUnitGroupSetService;
import org.nmcpye.datarun.iam.security.AuthoritiesConstants;
import org.nmcpye.datarun.web.common.ApiVersion;
import org.nmcpye.datarun.web.rest.legacy.JpaBaseResource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.nmcpye.datarun.web.rest.legacy.orgunit.OrgUnitGroupSetResource.CUSTOM;
import static org.nmcpye.datarun.web.rest.legacy.orgunit.OrgUnitGroupSetResource.V1;

/**
 * REST Extended controller for managing {@link OrgUnitGroupSet}.
 */
@RestController
@RequestMapping(value = {
    CUSTOM,
    V1})
@PreAuthorize("hasAnyAuthority(\"" + AuthoritiesConstants.ADMIN + "\", \"" + AuthoritiesConstants.USER + "\")")
public class OrgUnitGroupSetResource extends JpaBaseResource<OrgUnitGroupSet> {
    protected static final String NAME = "/orgUnitGroupSets";
    protected static final String CUSTOM = ApiVersion.API_CUSTOM + NAME;
    protected static final String V1 = ApiVersion.API_V1 + NAME;

    private final OrgUnitGroupSetService service;

    public OrgUnitGroupSetResource(OrgUnitGroupSetService service, OrgUnitGroupSetRepository repository) {
        super(service, repository);
        this.service = service;
    }

    @Override
    protected String getName() {
        return "orgUnitGroupSets";
    }


//    @Override
//    @PreAuthorize("hasAnyAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
//    public ResponseEntity<OrgUnitGroupSet> updateEntity(String uid, OrgUnitGroupSet entity) throws URISyntaxException {
//        return super.updateEntity(uid, entity);
//    }
//
//    @PreAuthorize("hasAnyAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
//    @Override
//    public ResponseEntity<?> saveReturnSaved(OrgUnitGroupSet entity) {
//        return super.saveReturnSaved(entity);
//    }
//
//    @PreAuthorize("hasAnyAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
//    @Override
//    public ResponseEntity<EntitySaveSummaryVM> saveOne(OrgUnitGroupSet entity) {
//        return super.saveOne(entity);
//    }
//
//    @PreAuthorize("hasAnyAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
//    @Override
//    public ResponseEntity<EntitySaveSummaryVM> saveAll(List<OrgUnitGroupSet> entities) {
//        return super.saveAll(entities);
//    }
}
