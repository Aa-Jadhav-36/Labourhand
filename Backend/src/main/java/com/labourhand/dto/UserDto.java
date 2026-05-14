package com.labourhand.dto;

import com.labourhand.model.User;
import lombok.Data;
import java.util.List;

@Data
public class UserDto {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private User.Role role;
    private String avatar;
    private boolean verified;
    private String language;

    // Worker-specific fields (null if owner)
    private String specialization;
    private int yearsExperience;
    private double rating;
    private int completedJobs;
    private String bio;
    private boolean skillsIndiaVerified;
    private double onTimeRate;
    private double rehireRate;
    private String workerStatus;
    private List<String> skills;

    // Payment Details
    private String paymentMethod;
    private String bankAccountNo;
    private String bankName;
    private String ifscCode;
    private String holderName;
    private String upiId;

    // Owner-specific fields (null if worker)
    private String companyName;
    private int projectsPosted;
}
