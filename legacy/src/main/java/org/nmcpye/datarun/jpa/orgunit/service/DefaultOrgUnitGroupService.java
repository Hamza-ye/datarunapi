package org.nmcpye.datarun.jpa.orgunit.service;

import jakarta.el.PropertyNotFoundException;
import org.nmcpye.datarun.jpa.accessfilter.UserAccessService;
import org.nmcpye.datarun.jpa.orgunit.OrgUnit;
import org.nmcpye.datarun.jpa.orgunit.OrgUnitGroup;
import org.nmcpye.datarun.jpa.orgunit.repository.OrgUnitGroupRepository;
import org.nmcpye.datarun.jpa.orgunit.repository.OrgUnitRepository;
import org.nmcpye.datarun.sharedkernal.DefaultJpaIdentifiableService;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
@Primary
@Transactional
public class DefaultOrgUnitGroupService extends DefaultJpaIdentifiableService<OrgUnitGroup> implements OrgUnitGroupService {

    private final OrgUnitRepository orgUnitRepository;

    public DefaultOrgUnitGroupService(OrgUnitGroupRepository repository, OrgUnitRepository orgUnitRepository, UserAccessService userAccessService, CacheManager cacheManager) {
        super(repository, cacheManager, userAccessService);
        this.orgUnitRepository = orgUnitRepository;
    }

    @Override
    public OrgUnitGroup saveWithRelations(OrgUnitGroup object) {
        if (!object.getOrgUnits().isEmpty()) {
            Set<OrgUnit> orgUnits = new HashSet<>();
            for (OrgUnit orgUnit : object.getOrgUnits()) {
                orgUnits.add(findOrgUnit(orgUnit));
            }

            object.setOrgUnits(orgUnits);
        }
        return save(object);
    }

    private OrgUnit findOrgUnit(OrgUnit orgUnit) {
        return Optional.ofNullable(orgUnit.getUid())
            .flatMap(orgUnitRepository::findByUid)
            .or(() -> Optional.ofNullable(orgUnit.getId())
                .flatMap(orgUnitRepository::findById))
            .or(() -> Optional.ofNullable(orgUnit.getCode())
                .flatMap(orgUnitRepository::findByCode))
            .orElseThrow(() -> new PropertyNotFoundException("OrgUnit not found: " + orgUnit));
    }
}
