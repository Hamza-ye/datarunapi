package org.nmcpye.datarun.template.datatemplate.repository;

import io.hypersistence.utils.spring.repository.BaseJpaRepository;
import org.nmcpye.datarun.template.datatemplate.CanonicalElement;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Hamza Assada
 * @since 25/09/2025
 */
@Repository
public interface CanonicalElementRepository extends BaseJpaRepository<CanonicalElement, String> {
    List<CanonicalElement> findByTemplateUid(String templateUid);
}
