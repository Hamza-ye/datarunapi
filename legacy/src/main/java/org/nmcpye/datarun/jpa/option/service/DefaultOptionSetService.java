package org.nmcpye.datarun.jpa.option.service;

import org.nmcpye.datarun.jpa.accessfilter.UserAccessService;
import org.nmcpye.datarun.jpa.option.OptionSet;
import org.nmcpye.datarun.jpa.option.repository.OptionSetRepository;
import org.nmcpye.datarun.sharedkernal.DefaultJpaIdentifiableService;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Primary
@Transactional
public class DefaultOptionSetService extends DefaultJpaIdentifiableService<OptionSet> implements OptionSetService {
    public DefaultOptionSetService(OptionSetRepository repository, CacheManager cacheManager, UserAccessService userAccessService) {
        super(repository, cacheManager, userAccessService);
    }
}
