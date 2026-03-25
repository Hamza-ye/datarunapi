package org.nmcpye.datarun.sharedkernal;

import io.hypersistence.utils.spring.repository.BaseJpaRepository;
import jakarta.validation.constraints.Size;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.ListPagingAndSortingRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@NoRepositoryBean
public interface JpaIdentifiableRepository<T extends JpaIdentifiableObject> extends BaseJpaRepository<T, String>,
    JpaSpecificationExecutor<T>,
    ListPagingAndSortingRepository<T, String>,
    IdentifiableObjectRepository<T, String> {

    Boolean existsByUid(@Size(max = 11) String uid);

    Optional<T> findByUid(@Size(max = 11) String uid);

    List<T> findAllByUidIn(Collection<String> uids);

    Page<T> findAllByUidIn(Collection<String> uids, Pageable pageable);
}
