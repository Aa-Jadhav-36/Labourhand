package com.labourhand.repository;

import com.labourhand.model.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {
    List<Rating> findByToUserId(Long toUserId);
    List<Rating> findByProjectId(Long projectId);
}
