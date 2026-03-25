package org.nmcpye.datarun.sharedkernal;

import org.nmcpye.datarun.sharedkernal.apiquery.QueryRequest;
import org.springframework.data.domain.Page;

import java.util.Optional;

/**
 * @author Hamza Assada 20/03/2025 (7amza.it@gmail.com)
 */
public interface IdentifiableObjectService<T extends IdentifiableObject<ID>, ID> {
    Class<T> getClazz();

    T saveWithRelations(T object);

    boolean existsByUid(String uid);

    Optional<T> findByUid(String uid);

    void deleteByUid(String uid);

    Page<T> findAllByUser(QueryRequest queryRequest, String jsonQueryBody);

    /**
     * Save an object.
     *
     * @param object the entity to save.
     * @return the persisted entity.
     */
    T save(T object);

    /**
     * Updates a object.
     *
     * @param object the entity to update.
     * @return the persisted entity.
     */
    T update(T object);

    /**
     * Delete an object.
     *
     * @param object the entity to delete.
     */
    void delete(T object);

    Optional<T> findByIdOrUid(T entity);

    Optional<T> findByIdOrUid(String entity);
}
