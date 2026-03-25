package org.nmcpye.datarun.web.rest.legacy.team;

import jakarta.validation.constraints.NotNull;
import org.nmcpye.datarun.iam.team.Team;
import org.nmcpye.datarun.iam.team.repository.TeamRepository;
import org.nmcpye.datarun.iam.team.service.TeamService;
import org.nmcpye.datarun.iam.security.AuthoritiesConstants;
import org.nmcpye.datarun.sharedkernal.apiquery.QueryRequest;
import org.nmcpye.datarun.web.common.ApiVersion;
import org.nmcpye.datarun.web.common.PagedResponse;
import org.nmcpye.datarun.web.errors.BadRequestAlertException;
import org.nmcpye.datarun.web.rest.legacy.JpaBaseResource;
import org.nmcpye.datarun.web.rest.v1.paging.PagingConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.nmcpye.datarun.web.rest.util.HeaderUtil;
import org.nmcpye.datarun.web.rest.util.ResponseUtil;

import java.net.URISyntaxException;
import java.util.Objects;
import java.util.Optional;

/**
 * Legacy REST controller for managing {@link Team}.
 * Admin/CUSTOM path only — the V1 mobile path is now served by
 * {@link org.nmcpye.datarun.web.rest.v1.team.TeamResource}.
 */
@RestController("teamResourceLegacy")
@RequestMapping(value = { TeamResource.CUSTOM })
@PreAuthorize("hasAnyAuthority(\"" + AuthoritiesConstants.ADMIN + "\", \"" + AuthoritiesConstants.USER + "\")")
public class TeamResource extends JpaBaseResource<Team> {
    protected static final String NAME = "/teams";
    protected static final String CUSTOM = ApiVersion.API_CUSTOM + NAME;

    private final Logger log = LoggerFactory.getLogger(TeamResource.class);

    private final TeamService teamService;

    private final TeamRepository teamRepository;

    public TeamResource(TeamService teamService,
            TeamRepository teamRepository) {
        super(teamService, teamRepository);
        this.teamService = teamService;
        this.teamRepository = teamRepository;
    }

    @Override
    protected String getName() {
        return "teams";
    }

    @GetMapping("managed")
    protected ResponseEntity<PagedResponse<?>> getAllManaged(
            QueryRequest queryRequest) {
        Pageable pageable = queryRequest.getPageable();

        Page<Team> processedPage = teamService.findAllManagedByUser(pageable, queryRequest);

        String next = PagingConfigurator.createNextPageLink(processedPage);

        PagedResponse<Team> response = PagingConfigurator.initPageResponse(processedPage, next, getName());
        return ResponseEntity.ok(response);
    }

    /**
     * {@code PATCH  /teams/:id} : Partial updates given fields of an existing team,
     * field will ignore if it is null
     *
     * @param uid  the id of the team to save.
     * @param team the team to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body
     *         the updated team,
     *         or with status {@code 400 (Bad Request)} if the team is not valid,
     *         or with status {@code 404 (Not Found)} if the team is not found,
     *         or with status {@code 500 (Internal Server Error)} if the team
     *         couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/teams/{uid}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<Team> partialUpdateTeam(
            @PathVariable(value = "uid", required = false) final String uid,
            @NotNull @RequestBody Team team) throws URISyntaxException {
        log.debug("REST request to partial update Team partially : {}, {}", uid, team);
        if (team.getUid() == null) {
            throw new BadRequestAlertException("Invalid uid", getName(), "idnull");
        }

        if (!Objects.equals(uid, team.getUid())) {
            throw new BadRequestAlertException("Invalid ID", getName(), "idinvalid");
        }

        if (!teamService.existsByUid(uid)) {
            throw new BadRequestAlertException("Entity not found", getName(), "idnotfound");
        }

        Optional<Team> result = teamService.partialUpdate(team);

        return ResponseUtil.wrapOrNotFound(
                result,
                HeaderUtil.createEntityUpdateAlert(applicationName, true, getName(), team.getUid()));
    }
}
