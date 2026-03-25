package org.nmcpye.datarun.jpa.orgunit.service;

import jakarta.el.PropertyNotFoundException;
import org.nmcpye.datarun.jpa.accessfilter.UserAccessService;
import org.nmcpye.datarun.jpa.orgunit.OrgUnit;
import org.nmcpye.datarun.jpa.orgunit.repository.OrgUnitRepository;
import org.nmcpye.datarun.sharedkernal.DefaultJpaIdentifiableService;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * @author Hamza Assada 18/01/2022
 */
@Service
@Primary
@Transactional
public class DefaultOrgUnitService extends DefaultJpaIdentifiableService<OrgUnit> implements OrgUnitService {

    private final OrgUnitRepository repository;
    private final OrgUnitMaintenanceService maintenanceService;
    private final org.springframework.context.ApplicationEventPublisher applicationEventPublisher;

    public DefaultOrgUnitService(OrgUnitRepository repository, UserAccessService userAccessService,
            CacheManager cacheManager, OrgUnitMaintenanceService maintenanceService,
            org.springframework.context.ApplicationEventPublisher applicationEventPublisher) {
        super(repository, cacheManager, userAccessService);
        this.repository = repository;
        this.maintenanceService = maintenanceService;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public OrgUnit save(OrgUnit object) {
        OrgUnit parent = object.getParent();
        if (parent != null) {
            parent = findParent(parent);

            // persistent instance references an unsaved transient instance
            parent.setPersisted(true);

            object.setParent(parent);
        }
        OrgUnit saved = repository.save(object);
        applicationEventPublisher.publishEvent(new org.nmcpye.datarun.party.events.OrgUnitSavedEvent(saved));
        return saved;
    }

    @Override
    public OrgUnit saveWithRelations(OrgUnit object) {
        OrgUnit parent = object.getParent();
        if (parent != null) {
            parent = findParent(parent);
            parent.setPersisted(true);
            object.setParent(parent);
        }
        return save(object);
    }

    private OrgUnit findParent(OrgUnit parent) {
        return Optional.ofNullable(parent.getId())
                .flatMap(repository::findById)
                .or(() -> Optional.ofNullable(parent.getUid())
                        .flatMap(repository::findByUid))
                .or(() -> Optional.ofNullable(parent.getCode())
                        .flatMap(repository::findByCode))
                // .map(OrgUnit::setIsPersisted)
                // .map(OrgUnit.class::cast)
                .orElseThrow(() -> new PropertyNotFoundException("Parent not found: " + parent));
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
    @Scheduled(cron = "0 0 4 * * ?")
    public void updatePaths() {
        maintenanceService.updateMissingPaths();
    }

    @Override
    @Transactional
    public void forceUpdatePaths() {
        maintenanceService.forceRecomputePaths();
    }

}
