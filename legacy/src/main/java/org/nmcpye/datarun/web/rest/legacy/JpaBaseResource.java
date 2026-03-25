package org.nmcpye.datarun.web.rest.legacy;

import lombok.extern.slf4j.Slf4j;

import org.nmcpye.datarun.sharedkernal.JpaIdentifiableObject;
import org.nmcpye.datarun.sharedkernal.JpaIdentifiableObjectService;
import org.nmcpye.datarun.sharedkernal.JpaIdentifiableRepository;
import org.nmcpye.datarun.web.common.BaseReadWriteResource;

@Slf4j
public abstract class JpaBaseResource<T extends JpaIdentifiableObject>
    extends BaseReadWriteResource<T, String> {
    protected final JpaIdentifiableObjectService<T> jpaAuditableObjectService;
    protected final JpaIdentifiableRepository<T> jpaIdentifiableRepository;

    protected JpaBaseResource(JpaIdentifiableObjectService<T> jpaAuditableObjectService,
                              JpaIdentifiableRepository<T> repository) {
        super(jpaAuditableObjectService, repository);
        this.jpaAuditableObjectService = jpaAuditableObjectService;
        this.jpaIdentifiableRepository = repository;
    }

    @Override
    protected JpaIdentifiableRepository<T> getRepository() {
        return (JpaIdentifiableRepository<T>) super.getRepository();
    }
}
