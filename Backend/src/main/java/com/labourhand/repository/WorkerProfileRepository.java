package com.labourhand.repository;

import com.labourhand.model.WorkerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface WorkerProfileRepository extends JpaRepository<WorkerProfile, Long> {

    @Query("SELECT w FROM WorkerProfile w WHERE w.status = :status")
    List<WorkerProfile> findByStatus(String status);
}
