package org.nmcpye.datarun.assignment.assignment.service;

import jakarta.el.PropertyNotFoundException;
import org.nmcpye.datarun.jpa.accessfilter.UserAccessService;
import org.nmcpye.datarun.assignment.activity.Activity;
import org.nmcpye.datarun.assignment.activity.repository.ActivityRepository;
import org.nmcpye.datarun.assignment.assignment.Assignment;
import org.nmcpye.datarun.assignment.assignment.dto.AssignmentWithAccessDto;
import org.nmcpye.datarun.assignment.assignment.mapper.AssignmentWithAccessMapper;
import org.nmcpye.datarun.assignment.assignment.repository.AssignmentRepository;
import org.nmcpye.datarun.datacapture.datasubmission.repository.DataSubmissionRepository;
import org.nmcpye.datarun.jpa.orgunit.OrgUnit;
import org.nmcpye.datarun.jpa.orgunit.repository.OrgUnitRepository;
import org.nmcpye.datarun.iam.team.Team;
import org.nmcpye.datarun.iam.team.repository.TeamRepository;
import org.nmcpye.datarun.party.service.AssignmentMaintenanceService;
import org.nmcpye.datarun.sharedkernal.DefaultJpaSoftDeleteService;
import org.nmcpye.datarun.sharedkernal.apiquery.QueryRequest;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.nmcpye.datarun.jpa.accessfilter.event.UserAccessRulesChangedEvent;
import org.springframework.context.ApplicationEventPublisher;

@Service
@Primary
@Transactional
public class DefaultAssignmentService
        extends DefaultJpaSoftDeleteService<Assignment>
        implements AssignmentService {

    private final AssignmentRepository repository;
    private final DataSubmissionRepository submissionRepository;
    private final TeamRepository teamRepository;
    private final ActivityRepository activityRepository;
    private final OrgUnitRepository orgUnitRepository;
    private final AssignmentMaintenanceService maintenanceService;
    private final AssignmentWithAccessMapper assignmentMapper;
    private final ApplicationEventPublisher applicationEventPublisher;

    public DefaultAssignmentService(AssignmentRepository repository,
            TeamRepository teamRepository,
            OrgUnitRepository orgUnitRepository,
            UserAccessService userAccessService,
            CacheManager cacheManager,
            AssignmentMaintenanceService maintenanceService,
            AssignmentWithAccessMapper assignmentMapper, AssignmentRepository assignmentRepository,
            DataSubmissionRepository submissionRepository, ActivityRepository activityRepository,
            ApplicationEventPublisher applicationEventPublisher) {
        super(repository, cacheManager, userAccessService, applicationEventPublisher);
        this.repository = repository;
        this.teamRepository = teamRepository;
        this.orgUnitRepository = orgUnitRepository;
        this.maintenanceService = maintenanceService;
        this.assignmentMapper = assignmentMapper;
        this.submissionRepository = submissionRepository;
        this.activityRepository = activityRepository;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public Assignment saveWithRelations(Assignment object) {

        Team team = null;
        Activity activity = null;
        OrgUnit orgUnit = null;

        if (object.getTeam() != null) {
            team = findTeam(object.getTeam());
        }

        if (object.getActivity() != null) {
            activity = findActivity(object.getActivity());
        }

        if (object.getOrgUnit() != null) {
            orgUnit = findOrgUnit(object.getOrgUnit());
        }

        Assignment parent = object.getParent();
        if (parent != null) {
            parent = findParent(parent);
            object.setParent(parent);
        }

        object.setActivity(activity);
        object.setTeam(team);
        object.setOrgUnit(orgUnit);

        Assignment saved = save(object);

        // Fire event for users active on this assignment's team
        if (saved.getTeam() != null) {
            saved.getTeam().getUsers().forEach(
                    u -> applicationEventPublisher.publishEvent(new UserAccessRulesChangedEvent(this, u.getLogin())));
        }

        return saved;
    }

    private Assignment findParent(Assignment parent) {
        return Optional.ofNullable(parent.getId()).flatMap(repository::findById)
                .or(() -> Optional.ofNullable(parent.getUid()).flatMap(repository::findByUid))
                .orElseThrow(() -> new PropertyNotFoundException("Parent not found: " + parent));
    }

    private Team findTeam(Team team) {
        return Optional.ofNullable(team.getId()).flatMap(teamRepository::findById)
                .or(() -> Optional.ofNullable(team.getUid())
                        .flatMap(teamRepository::findByUid))
                .or(() -> Optional.ofNullable(team.getCode())
                        .flatMap((code) -> teamRepository.findByCodeAndActivityUid(code, team.getActivity().getUid())))
                .orElseThrow(() -> new PropertyNotFoundException("Team not found: " + team));
    }

    private Activity findActivity(Activity activity) {
        return Optional.ofNullable(activity.getId())
                .flatMap(activityRepository::findById)
                .or(() -> Optional
                        .ofNullable(activity.getUid()).flatMap(activityRepository::findByUid))
                .orElseThrow(() -> new PropertyNotFoundException("Activity not found: " + activity));
    }

    private OrgUnit findOrgUnit(OrgUnit orgUnit) {
        return Optional.ofNullable(orgUnit.getId()).flatMap(orgUnitRepository::findById)
                .or(() -> Optional.ofNullable(orgUnit.getUid()).flatMap(orgUnitRepository::findByUid))
                .or(() -> Optional.ofNullable(orgUnit.getCode()).flatMap(orgUnitRepository::findByCode))
                .orElseThrow(() -> new PropertyNotFoundException("OrgUniy not found: " + orgUnit));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AssignmentWithAccessDto> getAllUserAccessibleDto(QueryRequest queryRequest, String jsonQueryBody) {
        Page<Assignment> assignedPage = findAllByUser(queryRequest, jsonQueryBody);
        return assignedPage.map(assignmentMapper::toDto);
    }

    List<Assignment> getAssignmentsWithChildren(Collection<String> uids) {
        List<Assignment> assignments = new ArrayList<>();
        for (String uid : uids) {
            assignments.addAll(repository.findAllByPathContaining(uid));
        }
        return assignments;
    }

    @Override
    public void updateStatusForSubmission(String submissionId) {
        final var submission = submissionRepository.findById(submissionId);

        submission.ifPresent(dataSubmission -> repository.findByUid(dataSubmission.getAssignment())
                .ifPresent(assignment -> {
                    assignment.setStatus(dataSubmission.getStatus());
                    assignment.setLastSubmittedBy(dataSubmission.getLastModifiedBy());
                    // * **`update`**: This method is for updating an entity that's
                    // already managed by the persistence context.
                    // The author claims this method is more performant than `merge`
                    // because it doesn't involve the same checks.
                    repository.update(assignment);
                }));
    }

    /**
     * Updates the paths of organization units in the system.
     * This method is scheduled to run automatically at 3:00 AM every day.
     * It ensures that the hierarchical paths of organization units are kept
     * up-to-date.
     * The method is transactional to ensure data consistency during the update
     * process.
     */
    @Override
    @Transactional
    @Scheduled(cron = "0 0 3 * * ?")
    public void updatePaths() {
        // repository.updatePaths();
        maintenanceService.updateMissingPaths();
    }

    @Override
    @Transactional
    public void forceUpdatePaths() {
        maintenanceService.forceRecomputePaths();
        // repository.forceUpdatePaths();
    }
}
