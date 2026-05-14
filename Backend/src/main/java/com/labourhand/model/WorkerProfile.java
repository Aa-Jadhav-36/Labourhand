package com.labourhand.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "worker_profiles")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class WorkerProfile {

    @Id
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    private String specialization;

    @Column(name = "years_experience")
    private int yearsExperience;

    private double rating = 0.0;

    @Column(name = "completed_jobs")
    private int completedJobs = 0;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(name = "skills_india_verified")
    private boolean skillsIndiaVerified = false;

    @Column(name = "on_time_rate")
    private double onTimeRate = 0.0;

    @Column(name = "rehire_rate")
    private double rehireRate = 0.0;

    // Worker status: "available" or "on-site"
    private String status = "available";

    @Column(name = "current_site")
    private String currentSite;

    // --- Payment Details ---
    @Column(name = "payment_method")
    private String paymentMethod; // "BANK" or "UPI"

    @Column(name = "bank_account_no")
    private String bankAccountNo;

    @Column(name = "bank_name")
    private String bankName;

    @Column(name = "ifsc_code")
    private String ifscCode;

    @Column(name = "holder_name")
    private String holderName;

    @Column(name = "upi_id")
    private String upiId;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "worker_skills",
        joinColumns = @JoinColumn(name = "worker_id"),
        inverseJoinColumns = @JoinColumn(name = "skill_id")
    )
    @Builder.Default
    private List<Skill> skills = new ArrayList<>();

    @OneToMany(mappedBy = "workerProfile", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Certification> certifications = new ArrayList<>();
}
