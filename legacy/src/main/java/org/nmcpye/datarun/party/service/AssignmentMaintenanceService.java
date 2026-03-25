package org.nmcpye.datarun.party.service;

import jakarta.persistence.EntityManager;
import org.nmcpye.datarun.assignment.assignment.Assignment;
import org.nmcpye.datarun.assignment.assignment.repository.AssignmentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * @author Hamza Assada 20/03/2023
 */
@Service
public class AssignmentMaintenanceService {
    private static final int CHUNK_SIZE = 400;

    private final AssignmentRepository repository;
    private final EntityManager em;
    private final PlatformTransactionManager txm;

    public AssignmentMaintenanceService(AssignmentRepository repository,
            EntityManager em,
            PlatformTransactionManager txm) {
        this.repository = repository;
        this.em = em;
        this.txm = txm;
    }

    public void updateMissingPaths() {
        processInChunks(false);
    }

    public void forceRecomputePaths() {
        processInChunks(true);
    }

    private void processInChunks(boolean force) {
        int page = 0;
        Page<Assignment> chunk;
        do {
            Pageable pg = PageRequest.of(page, CHUNK_SIZE);
            chunk = force
                    ? repository.findAll(pg)
                    : repository.findAllByPathIsNull(pg);

            if (!chunk.isEmpty()) {
                // run each chunk in its own transaction to keep EM small
                Page<Assignment> finalChunk = chunk;
                new TransactionTemplate(txm).execute(status -> {
                    for (Assignment ou : finalChunk) {
                        // calling getter will recompute
                        ou.setPath(ou.getPath());
                        ou.setLevel(ou.getLevel());
                    }
                    repository.saveAll(finalChunk.getContent());
                    // make sure we don’t accumulate managed state
                    em.flush();
                    em.clear();
                    return null;
                });
            }
            page++;
        } while (!chunk.isLast());
    }
}
