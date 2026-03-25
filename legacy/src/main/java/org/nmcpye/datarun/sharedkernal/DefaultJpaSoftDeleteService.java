package org.nmcpye.datarun.sharedkernal;

import lombok.extern.slf4j.Slf4j;

import org.nmcpye.datarun.assignment.assignment.Assignment;
import org.nmcpye.datarun.iam.team.Team;
import org.nmcpye.datarun.iam.usegroup.UserGroup;
import org.nmcpye.datarun.jpa.accessfilter.UserAccessService;
import org.nmcpye.datarun.sharedkernal.apiquery.QueryRequest;
import org.nmcpye.datarun.sharedkernal.apiquery.filter.*;
import org.springframework.cache.CacheManager;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.springframework.context.ApplicationEventPublisher;

/**
 * @author Hamza Assada
 * @since 20/03/2025
 */
@Slf4j
public abstract class DefaultJpaSoftDeleteService<T extends JpaSoftDeleteObject>
        extends DefaultJpaIdentifiableService<T>
        implements SoftDeleteService<T, String> {

    protected final UserAccessService userAccessService;
    protected final JpaIdentifiableRepository<T> jpaAuditableObjectRepository;
    protected final ApplicationEventPublisher applicationEventPublisher;

    public DefaultJpaSoftDeleteService(JpaIdentifiableRepository<T> jpaAuditableObjectRepository,
            CacheManager cacheManager, UserAccessService userAccessService,
            ApplicationEventPublisher applicationEventPublisher) {
        super(jpaAuditableObjectRepository, cacheManager, userAccessService);
        this.userAccessService = userAccessService;
        this.jpaAuditableObjectRepository = jpaAuditableObjectRepository;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Transactional
    @Override
    public void deleteByUid(String uid) {
        log.debug("Request soft delete service to soft delete {}:`{}`", getClazz().getSimpleName(), uid);
        findByIdOrUid(uid).ifPresent(this::softDelete);
    }

    @Transactional
    @Override
    public void delete(T object) {
        log.debug("Request soft delete service to soft delete {}:`{}`", getClazz().getSimpleName(), object.getUid());
        findByIdOrUid(object).ifPresent(this::softDelete);
    }

    @Override
    public void softDelete(T object) {
        if (object.getDeleted())
            return;
        object.setDeletedAt(Instant.now());

        // Fire access rules changed event if the soft-deleted entity affects user
        // permissions
        if (applicationEventPublisher != null) {
            Object obj = object;
            if (obj instanceof Team team) {
                team.getUsers().forEach(u -> applicationEventPublisher.publishEvent(
                        new org.nmcpye.datarun.jpa.accessfilter.event.UserAccessRulesChangedEvent(this, u.getLogin())));
            } else if (obj instanceof Assignment assignment) {
                if (assignment.getTeam() != null) {
                    assignment.getTeam().getUsers()
                            .forEach(u -> applicationEventPublisher.publishEvent(
                                    new org.nmcpye.datarun.jpa.accessfilter.event.UserAccessRulesChangedEvent(this,
                                            u.getLogin())));
                }
            } else if (obj instanceof UserGroup userGroup) {
                userGroup.getUsers().forEach(u -> applicationEventPublisher.publishEvent(
                        new org.nmcpye.datarun.jpa.accessfilter.event.UserAccessRulesChangedEvent(this, u.getLogin())));
            }
        }

        save(object);
    }

    @Override
    protected FilterExpression buildCombinedFilter(QueryRequest queryRequest, String jsonQueryBody) {
        List<FilterExpression> allFilters = new ArrayList<>();
        final FilterExpression baseFilter = super.buildCombinedFilter(queryRequest, jsonQueryBody);
        if (baseFilter != null) {
            allFilters.add(baseFilter);
        }

        /// ///
        // Implicit soft-delete filter
        if (queryRequest == null || !queryRequest.isIncludeDeleted()) {
            allFilters.add(new SimpleFilter("deleted", FilterOperator.EQ, false));
        }
        //

        if (allFilters.isEmpty())
            return null;
        if (allFilters.size() == 1)
            return allFilters.get(0);

        return new CompoundFilter(LogicalOperator.AND, allFilters);
    }
}
