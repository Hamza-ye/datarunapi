package org.nmcpye.datarun.template.datatemplate.repository;

import io.hypersistence.utils.spring.repository.BaseJpaRepository;
import org.nmcpye.datarun.template.datatemplate.TemplateElement;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TemplateElementRepository
    extends BaseJpaRepository<TemplateElement, Long> {
    Optional<TemplateElement> findByUid(String uid);
}
