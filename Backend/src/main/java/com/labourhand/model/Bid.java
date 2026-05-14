package com.labourhand.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
@Entity
@Table(name = "bids")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bid {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "worker_id", nullable = false)
    private Long workerId;

    @Column(nullable = false)
    private long amount; // bid amount in INR

    @Column(name = "amount_paid", nullable = false)
    private long amountPaid = 0; // tracked amount already paid to worker

    @Column(name = "estimated_days", nullable = false)
    private int estimatedDays;

    @Column(columnDefinition = "TEXT")
    private String message;

    @ElementCollection
    @CollectionTable(name = "bid_team_workers", joinColumns = @JoinColumn(name = "bid_id"))
    @Column(name = "worker_id")
    private List<Long> teamWorkerIds = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.PENDING;

    private boolean recommended = false;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt = LocalDateTime.now();

    public enum Status {
        PENDING,
        ACCEPTED,
        REJECTED
    }
}
