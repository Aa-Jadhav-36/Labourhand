package com.labourhand.repository;

import com.labourhand.model.Bid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface BidRepository extends JpaRepository<Bid, Long> {

    List<Bid> findByProjectId(Long projectId);

    List<Bid> findByWorkerId(Long workerId);

    List<Bid> findByWorkerIdAndStatus(Long workerId, Bid.Status status);

    Optional<Bid> findByProjectIdAndWorkerId(Long projectId, Long workerId);

    // Get distinct worker IDs who've been accepted on the owner's projects
    @Query("""
            SELECT DISTINCT b.workerId FROM Bid b
            JOIN Project p ON p.id = b.projectId
            WHERE p.ownerId = :ownerId AND b.status = 'ACCEPTED'
            """)
    List<Long> findAcceptedWorkerIdsByOwnerId(@Param("ownerId") Long ownerId);
}
