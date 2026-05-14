package com.labourhand.repository;

import com.labourhand.model.ScheduleEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface ScheduleEventRepository extends JpaRepository<ScheduleEvent, Long> {
    List<ScheduleEvent> findByWorkerIdOrderByDateAsc(Long workerId);

    List<ScheduleEvent> findByProjectIdOrderByDateAsc(Long projectId);

    List<ScheduleEvent> findByDateGreaterThanEqualOrderByDateAsc(LocalDate from);
}
