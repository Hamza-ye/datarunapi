package org.nmcpye.datarun.jpa.orgunit.service;

import org.nmcpye.datarun.jpa.accessfilter.UserAccessService;
import org.nmcpye.datarun.jpa.orgunit.OuLevel;
import org.nmcpye.datarun.jpa.orgunit.repository.OuLevelRepository;
import org.nmcpye.datarun.sharedkernal.DefaultJpaIdentifiableService;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Primary
@Transactional
public class DefaultOuLevelService extends DefaultJpaIdentifiableService<OuLevel> implements OuLevelService {

    public DefaultOuLevelService(OuLevelRepository repository, CacheManager cacheManager, UserAccessService userAccessService) {
        super(repository, cacheManager, userAccessService);
    }
}
