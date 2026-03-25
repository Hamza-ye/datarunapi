package org.nmcpye.datarun.iam.usegroup.service;

import org.nmcpye.datarun.iam.usegroup.UserGroup;
import org.nmcpye.datarun.sharedkernal.JpaIdentifiableObjectService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link UserGroup}.
 */
public interface UserGroupService
    extends JpaIdentifiableObjectService<UserGroup> {

    Page<UserGroup> findAllManagedByUser(Pageable pageable);
}
