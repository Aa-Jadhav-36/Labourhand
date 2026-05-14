package com.labourhand.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "projects")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String category; // Construction, Plumbing, Electrical, Painting, Carpentry, Masonry

    @Column(nullable = false)
    private long budget; // in INR (paise or rupees)

    @Column(name = "timeline_days")
    private int timelineDays;

    @Column(nullable = false)
    private String location;

    private Double lat;
    private Double lng;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.OPEN_FOR_BIDS;

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @Column(name = "accepted_bid_id")
    private Long acceptedBidId;

    // 0-100 progress percentage
    private int progress = 0;

    private LocalDate deadline;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum Status {
        OPEN_FOR_BIDS,
        IN_PROGRESS,
        COMPLETED,
        PAYMENT_VERIFIED
    }
}
