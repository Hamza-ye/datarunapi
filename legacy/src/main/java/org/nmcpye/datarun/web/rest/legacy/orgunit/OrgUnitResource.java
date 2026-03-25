package org.nmcpye.datarun.web.rest.legacy.orgunit;

import org.nmcpye.datarun.jpa.orgunit.OrgUnit;
import org.nmcpye.datarun.jpa.orgunit.repository.OrgUnitRepository;
import org.nmcpye.datarun.jpa.orgunit.service.OrgUnitService;
import org.nmcpye.datarun.iam.security.AuthoritiesConstants;
import org.nmcpye.datarun.iam.security.SecurityUtils;
import org.nmcpye.datarun.sharedkernal.exceptions.IllegalQueryException;
import org.nmcpye.datarun.sharedkernal.feedback.ErrorCode;
import org.nmcpye.datarun.sharedkernal.feedback.ErrorMessage;
import org.nmcpye.datarun.web.common.ApiVersion;
import org.nmcpye.datarun.web.errors.PathUpdateException;
import org.nmcpye.datarun.web.rest.legacy.JpaBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static org.nmcpye.datarun.web.rest.legacy.orgunit.OrgUnitResource.CUSTOM;

/**
 * Legacy REST controller for managing {@link OrgUnit}.
 * Admin/CUSTOM path only — the V1 mobile path is now served by
 * {@link org.nmcpye.datarun.web.rest.v1.orgunit.OrgUnitResource}.
 */
@RestController("orgUnitResourceLegacy")
@RequestMapping(value = { CUSTOM })
@PreAuthorize("hasAnyAuthority(\"" + AuthoritiesConstants.ADMIN + "\", \"" + AuthoritiesConstants.USER + "\")")
public class OrgUnitResource extends JpaBaseResource<OrgUnit> {
    protected static final String NAME = "/orgUnits";
    protected static final String CUSTOM = ApiVersion.API_CUSTOM + NAME;

    private final Logger log = LoggerFactory.getLogger(OrgUnitResource.class);

    private final OrgUnitService serviceCustom;

    private final OrgUnitRepository repositoryCustom;

    public OrgUnitResource(OrgUnitService serviceCustom, OrgUnitRepository repositoryCustom) {
        super(serviceCustom, repositoryCustom);
        this.repositoryCustom = repositoryCustom;
        this.serviceCustom = serviceCustom;
    }

    @Override
    protected String getName() {
        return "orgUnits";
    }

    /**
     * {@code GET  /Ts} : updatePaths?forceUpdate=true update paths.
     *
     * @param forceUpdate force update paths of all.
     */
    @GetMapping("/updatePaths")
    public ResponseEntity<String> updateTeam(
            @RequestParam(name = "forceUpdate", required = false, defaultValue = "false") boolean forceUpdate) {
        log.debug("REST request to update orgUnit Paths");
        final var user = SecurityUtils.getCurrentUserDetailsOrThrow();
        hasMinimalRightsOrThrow(user);
        try {
            if (forceUpdate) {
                serviceCustom.forceUpdatePaths();
            } else {
                serviceCustom.updatePaths();
            }
            return ResponseEntity.ok("Paths updated successfully");
        } catch (Exception e) {
            log.error("Error occurred while updating paths", e);
            throw new IllegalQueryException(new ErrorMessage(ErrorCode.E1003, e.getMessage()));
        }
    }

    @ExceptionHandler(PathUpdateException.class)
    public ResponseEntity<String> handlePathUpdateException(PathUpdateException ex) {
        log.error("Handling PathUpdateException: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
    }

    // @Override
    // @PreAuthorize("hasAnyAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    // public ResponseEntity<OrgUnit> updateEntity(String uid, OrgUnit entity)
    // throws URISyntaxException {
    // return super.updateEntity(uid, entity);
    // }
    //
    // @PreAuthorize("hasAnyAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    // @Override
    // public ResponseEntity<?> saveReturnSaved(OrgUnit entity) {
    // return super.saveReturnSaved(entity);
    // }
    //
    // @PreAuthorize("hasAnyAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    // @Override
    // public ResponseEntity<EntitySaveSummaryVM> saveOne(OrgUnit entity) {
    // return super.saveOne(entity);
    // }
    //
    // @PreAuthorize("hasAnyAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    // @Override
    // public ResponseEntity<EntitySaveSummaryVM> saveAll(List<OrgUnit> entities) {
    // return super.saveAll(entities);
    // }
}
