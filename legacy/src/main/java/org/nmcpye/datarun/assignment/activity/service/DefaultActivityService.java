package org.nmcpye.datarun.assignment.activity.service;

import org.nmcpye.datarun.jpa.accessfilter.UserAccessService;
import org.nmcpye.datarun.assignment.activity.Activity;
import org.nmcpye.datarun.assignment.activity.repository.ActivityRepository;
import org.nmcpye.datarun.iam.security.AuthoritiesConstants;
import org.nmcpye.datarun.iam.security.SecurityUtils;
import org.nmcpye.datarun.sharedkernal.DefaultJpaIdentifiableService;
import org.nmcpye.datarun.sharedkernal.apiquery.QueryRequest;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Hamza Assada 11/02/2022
 */
@Service
@Primary
@Transactional
public class DefaultActivityService
        extends DefaultJpaIdentifiableService<Activity>
        implements ActivityService {

    private final ActivityRepository repository;
    private final org.springframework.context.ApplicationEventPublisher applicationEventPublisher;

    public DefaultActivityService(ActivityRepository repository, CacheManager cacheManager,
            UserAccessService userAccessService,
            org.springframework.context.ApplicationEventPublisher applicationEventPublisher) {
        super(repository, cacheManager, userAccessService);
        this.repository = repository;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public Page<Activity> findAllByUser(QueryRequest queryRequest, String jsonQueryBody) {
        Pageable pageable = queryRequest.getPageable();

        if (SecurityUtils.hasCurrentUserAnyOfAuthorities(AuthoritiesConstants.ADMIN)) {
            return repository.findAll(pageable);
        }
        return repository.findAllByUser(pageable);
    }

    @Override
    public Activity save(Activity activity) {
        Activity saved = super.save(activity);
        applicationEventPublisher.publishEvent(new org.nmcpye.datarun.party.events.ActivitySavedEvent(saved));
        return saved;
    }

    private void clearCaches(Activity activity) {
        // team.getUsers().forEach(user -> {
        // this.clearCaches(UserRepository.USERS_BY_LOGIN_CACHE, user.getLogin());
        // this.clearCaches(UserRepository.USERS_BY_EMAIL_CACHE, user.getEmail());
        // this.clearCaches(UserRepository.USER_TEAM_IDS_CACHE, user.getLogin());
        // this.clearCaches(UserRepository.USER_GROUP_IDS_CACHE, user.getLogin());
        // });
    }
}
