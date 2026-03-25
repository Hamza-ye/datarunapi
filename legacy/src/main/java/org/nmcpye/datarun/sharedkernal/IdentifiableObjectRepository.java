package org.nmcpye.datarun.sharedkernal;

import jakarta.validation.constraints.Size;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@NoRepositoryBean
public interface IdentifiableObjectRepository<T extends IdentifiableObject<ID>, ID>
    extends CrudRepository<T, ID> {

    Boolean existsByUid(@Size(max = 11) String uid);

    Optional<T> findByUid(@Size(max = 11) String uid);

    List<T> findAllByUidIn(Collection<String> uids);

    Page<T> findAllByUidIn(Collection<String> uids, Pageable pageable);
}
