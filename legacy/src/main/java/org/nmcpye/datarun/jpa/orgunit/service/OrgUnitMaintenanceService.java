package org.nmcpye.datarun.jpa.orgunit.service;

import jakarta.persistence.EntityManager;
import org.nmcpye.datarun.assignment.assignment.repository.AssignmentRepository;
import org.nmcpye.datarun.jpa.orgunit.OrgUnit;
import org.nmcpye.datarun.jpa.orgunit.repository.OrgUnitRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * @author Hamza Assada 18/01/2022
 */
@Service
public class OrgUnitMaintenanceService {
    private static final int CHUNK_SIZE = 400;

    private final OrgUnitRepository orgUnitRepository;
    private final AssignmentRepository flowInstanceRepository;
    private final EntityManager em;
    private final PlatformTransactionManager txm;

    public OrgUnitMaintenanceService(OrgUnitRepository orgUnitRepository, AssignmentRepository flowInstanceRepository,
                                     EntityManager em,
                                     PlatformTransactionManager txm) {
        this.orgUnitRepository = orgUnitRepository;
        this.flowInstanceRepository = flowInstanceRepository;
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
        Page<OrgUnit> chunk;
        do {
            Pageable pg = PageRequest.of(page, CHUNK_SIZE);
            chunk = force
                ? orgUnitRepository.findAll(pg)
                : orgUnitRepository.findAllByPathIsNull(pg);

            if (!chunk.isEmpty()) {
                // run each chunk in its own transaction to keep EM small
                Page<OrgUnit> finalChunk = chunk;
                new TransactionTemplate(txm).execute(status -> {
                    for (OrgUnit ou : finalChunk) {
                        // calling getter will recompute
                        ou.setPath(ou.getPath());
                        ou.setLevel(ou.getLevel());
                    }
                    orgUnitRepository.saveAll(finalChunk.getContent());
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
