package com.labourhand.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "progress_updates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProgressUpdate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "user_id", nullable = false)
    private Long userId; // The person who posted the update (Worker or Employer)

    @Column(name = "progress_percentage")
    private Integer progressPercentage; // e.g. 25 meaning 25%

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(name = "payment_demanded")
    private Boolean paymentDemanded;

    @Column(name = "demand_amount")
    private Double demandAmount;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
