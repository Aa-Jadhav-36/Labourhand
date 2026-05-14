package com.labourhand.repository;

import com.labourhand.model.Earning;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

public interface EarningRepository extends JpaRepository<Earning, Long> {
    List<Earning> findByOwnerIdAndDateBetweenOrderByDateAsc(Long ownerId, LocalDate from, LocalDate to);

    @Query("SELECT SUM(e.amount) FROM Earning e WHERE e.ownerId = :ownerId AND e.date BETWEEN :from AND :to")
    Long sumByOwnerIdAndDateRange(@Param("ownerId") Long ownerId, @Param("from") LocalDate from,
            @Param("to") LocalDate to);
}
