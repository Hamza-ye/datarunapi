package org.nmcpye.datarun.iam.usegroup.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.nmcpye.datarun.iam.usegroup.UserGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

/**
 * Utility repository to load bag relationships based on https://vladmihalcea.com/hibernate-multiplebagfetchexception/
 */
public class UserGroupRepositoryWithBagRelationshipsImpl implements UserGroupRepositoryWithBagRelationships {

    private static final String ID_PARAMETER = "id";
    private static final String TEAMS_PARAMETER = "groups";

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<UserGroup> fetchBagRelationships(Page<UserGroup> userGroups) {
        return new PageImpl<>(fetchBagRelationships(userGroups.getContent()), userGroups.getPageable(), userGroups.getTotalElements());
    }

    @Override
    public List<UserGroup> fetchBagRelationships(List<UserGroup> userGroups) {
        return Optional.of(userGroups).map(this::fetchUserGroups).orElse(Collections.emptyList());
    }

    UserGroup fetchUserGroups(UserGroup result) {
        return entityManager
            .createQuery("select ug from UserGroup ug " +
                "left join fetch ug.users user " +
                "where ug.id = :id", UserGroup.class)
            .setParameter(ID_PARAMETER, result.getId())
            .getSingleResult();
    }

    List<UserGroup> fetchUserGroups(List<UserGroup> userGroups) {
        HashMap<Object, Integer> order = new HashMap<>();
        IntStream.range(0, userGroups.size()).forEach(index -> order.put(userGroups.get(index).getId(), index));
        List<UserGroup> result = entityManager
            .createQuery("select ug from UserGroup ug " +
                "left join fetch ug.users user " +
                "where ug in :userGroups", UserGroup.class)
            .setParameter(TEAMS_PARAMETER, userGroups)
            .getResultList();
        result.sort((o1, o2) -> Integer.compare(order.get(o1.getId()), order.get(o2.getId())));
        return result;
    }
}
