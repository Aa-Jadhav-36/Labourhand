package com.labourhand.repository;

import com.labourhand.model.Certification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CertificationRepository extends JpaRepository<Certification, Long> {
    List<Certification> findByWorkerProfileId(Long workerId);

    void deleteByWorkerProfileIdAndId(Long workerId, Long certId);
}
