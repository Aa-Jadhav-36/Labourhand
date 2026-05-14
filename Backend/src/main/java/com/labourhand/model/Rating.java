package com.labourhand.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ratings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "from_user_id", nullable = false)
    private Long fromUserId; // Payer (Employer)

    @Column(name = "to_user_id", nullable = false)
    private Long toUserId; // Payee (Worker)

    @Column(nullable = false)
    private Double rating; // 1 to 5

    @Column(length = 1000)
    private String comment;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
