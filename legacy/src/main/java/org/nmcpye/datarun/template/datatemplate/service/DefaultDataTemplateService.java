package org.nmcpye.datarun.template.datatemplate.service;

import org.nmcpye.datarun.jpa.accessfilter.UserAccessService;
import org.nmcpye.datarun.template.datatemplate.DataTemplate;
import org.nmcpye.datarun.template.datatemplate.repository.DataTemplateRepository;
import org.nmcpye.datarun.sharedkernal.DefaultJpaSoftDeleteService;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/// Service Implementation for managing [DataTemplate].
@Service
@Primary
@Transactional
public class DefaultDataTemplateService
        extends DefaultJpaSoftDeleteService<DataTemplate>
        implements DataTemplateService {

    public DefaultDataTemplateService(DataTemplateRepository repository,
            CacheManager cacheManager,
            UserAccessService userAccessService,
            ApplicationEventPublisher applicationEventPublisher) {
        super(repository, cacheManager, userAccessService, applicationEventPublisher);
    }
}
