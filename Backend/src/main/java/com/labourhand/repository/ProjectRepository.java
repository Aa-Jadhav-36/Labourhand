package com.labourhand.repository;

import com.labourhand.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findByOwnerId(Long ownerId);

    List<Project> findByStatus(Project.Status status);

    List<Project> findByCategory(String category);

    List<Project> findByOwnerIdAndStatus(Long ownerId, Project.Status status);

    /** Returns only OPEN_FOR_BIDS projects of a given category. */
    List<Project> findByCategoryAndStatus(String category, Project.Status status);

    // Returns OPEN_FOR_BIDS projects sorted by distance (Haversine).
    // Guards against NULL lat/lng to prevent ACOS math errors.
    @Query(value = """
            SELECT id, distance FROM (
                SELECT id,
                    6371 * ACOS(
                        LEAST(1.0, GREATEST(-1.0,
                            COS(RADIANS(:lat)) * COS(RADIANS(lat)) *
                            COS(RADIANS(lng) - RADIANS(:lng)) +
                            SIN(RADIANS(:lat)) * SIN(RADIANS(lat))
                        ))
                    ) AS distance
                FROM projects
                WHERE status = 'OPEN_FOR_BIDS'
                  AND lat IS NOT NULL
                  AND lng IS NOT NULL
            ) AS sub
            WHERE distance <= :radius
            ORDER BY distance ASC
            LIMIT 50
            """, nativeQuery = true)
    List<Object[]> findNearbyProjectsRaw(@Param("lat") double lat, @Param("lng") double lng, @Param("radius") double radius);

    // Returns OPEN_FOR_BIDS projects of a given category sorted by distance.
    @Query(value = """
            SELECT id, distance FROM (
                SELECT id,
                    6371 * ACOS(
                        LEAST(1.0, GREATEST(-1.0,
                            COS(RADIANS(:lat)) * COS(RADIANS(lat)) *
                            COS(RADIANS(lng) - RADIANS(:lng)) +
                            SIN(RADIANS(:lat)) * SIN(RADIANS(lat))
                        ))
                    ) AS distance
                FROM projects
                WHERE status = 'OPEN_FOR_BIDS'
                  AND lat IS NOT NULL
                  AND lng IS NOT NULL
                  AND UPPER(category) = UPPER(:category)
            ) AS sub
            WHERE distance <= :radius
            ORDER BY distance ASC
            LIMIT 50
            """, nativeQuery = true)
    List<Object[]> findNearbyProjectsByCategoryRaw(
            @Param("lat") double lat,
            @Param("lng") double lng,
            @Param("radius") double radius,
            @Param("category") String category);
}
