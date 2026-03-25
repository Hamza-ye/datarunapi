package org.nmcpye.datarun.datacapture.datasubmission.repository;

import org.nmcpye.datarun.datacapture.datasubmission.DataSubmission;
import org.nmcpye.datarun.sharedkernal.JpaIdentifiableRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DataSubmissionRepository
    extends JpaIdentifiableRepository<DataSubmission> {

    Page<DataSubmission> findBySerialNumberGreaterThanAndFormInOrderBySerialNumberAsc(Long lastSerial, List<String> uids, Pageable pageable);

    List<DataSubmission> findBySerialNumberBetween(Long serialNumberAfter, Long serialNumberBefore);

    Integer countBySerialNumberGreaterThan(Long serialNumberIsGreaterThan);
}
