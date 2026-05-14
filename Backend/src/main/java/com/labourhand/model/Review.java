package com.labourhand.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reviews")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "reviewer_id", nullable = false)
    private Long reviewerId; // owner who reviews

    @Column(name = "reviewee_id", nullable = false)
    private Long revieweeId; // worker being reviewed

    @Column(nullable = false)
    private double rating; // 1.0 - 5.0

    @Column(columnDefinition = "TEXT")
    private String comment;

    private String duration;

    @Column(name = "project_name")
    private String projectName;

    @Column(name = "client_name")
    private String clientName;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
