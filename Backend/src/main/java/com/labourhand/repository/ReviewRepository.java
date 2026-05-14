package com.labourhand.repository;

import com.labourhand.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByRevieweeIdOrderByCreatedAtDesc(Long revieweeId);

    List<Review> findByReviewerId(Long reviewerId);

    List<Review> findByProjectId(Long projectId);
}
