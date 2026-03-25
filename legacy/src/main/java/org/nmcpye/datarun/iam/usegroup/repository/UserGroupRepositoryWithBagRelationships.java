package org.nmcpye.datarun.iam.usegroup.repository;

import org.nmcpye.datarun.iam.usegroup.UserGroup;
import org.springframework.data.domain.Page;

import java.util.List;

public interface UserGroupRepositoryWithBagRelationships {

    List<UserGroup> fetchBagRelationships(List<UserGroup> groups);

    Page<UserGroup> fetchBagRelationships(Page<UserGroup> groups);
}
