package org.nmcpye.datarun.sharedkernal;

import org.springframework.transaction.annotation.Transactional;

/**
 * @author Hamza Assada 20/03/2025 (7amza.it@gmail.com)
 */
@Transactional
public interface SoftDeleteService<T extends SoftDeleteObject<ID>, ID> {
    void softDelete(T object);
}
