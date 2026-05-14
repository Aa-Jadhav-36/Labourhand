package com.labourhand.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "owner_profiles")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OwnerProfile {

    @Id
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "company_name")
    private String companyName;

    @Column(name = "projects_posted")
    private int projectsPosted = 0;
}
