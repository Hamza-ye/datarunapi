package org.nmcpye.datarun.web.rest.legacy.activity;

import org.nmcpye.datarun.assignment.activity.Activity;
import org.nmcpye.datarun.assignment.activity.repository.ActivityRepository;
import org.nmcpye.datarun.assignment.activity.service.ActivityService;
import org.nmcpye.datarun.iam.security.AuthoritiesConstants;
import org.nmcpye.datarun.web.common.ApiVersion;
import org.nmcpye.datarun.web.rest.legacy.JpaBaseResource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.nmcpye.datarun.web.rest.legacy.activity.ActivityResource.CUSTOM;
import static org.nmcpye.datarun.web.rest.legacy.activity.ActivityResource.V1;

/**
 * REST Extended controller for managing {@link Activity}.
 */
@RestController("activityResourceLegacy")
@RequestMapping(value = {CUSTOM, V1})
@PreAuthorize("hasAnyAuthority(\"" + AuthoritiesConstants.ADMIN + "\", \"" + AuthoritiesConstants.USER + "\")")
public class ActivityResource extends JpaBaseResource<Activity> {

    protected static final String NAME = "/activities";
    protected static final String CUSTOM = ApiVersion.API_CUSTOM + NAME;
    protected static final String V1 = ApiVersion.API_V1 + NAME;

    public ActivityResource(ActivityService activityService, ActivityRepository activityRepository) {
        super(activityService, activityRepository);
    }

    @Override
    protected String getName() {
        return "activities";
    }
}
