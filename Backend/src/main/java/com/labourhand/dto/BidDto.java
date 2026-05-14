package com.labourhand.dto;

import lombok.Data;
import jakarta.validation.constraints.*;
import java.util.List;
import java.util.ArrayList;

public class BidDto {

    @Data
    public static class Request {
        @NotNull
        private Long projectId;
        @Positive
        private long amount;
        @Min(1)
        private int estimatedDays;
        @NotBlank
        @Size(max = 500)
        private String message;
        private List<Long> teamWorkerIds = new ArrayList<>();
    }

    @Data
    public static class Response {
        private Long id;
        private Long projectId;
        private String projectTitle;
        private String projectLocation;
        private Long workerId;
        private String workerName;
        private String workerAvatar;
        private double workerRating;
        private int workerCompletedJobs;
        private boolean workerVerified;
        private long amount;
        private long amountPaid;
        private long amountRemaining;
        private int estimatedDays;
        private String message;
        private List<Long> teamWorkerIds = new ArrayList<>();
        private String status; // PENDING, ACCEPTED, REJECTED
        private boolean recommended;
        private String submittedAt;
        // Worker-side view extras
        private int progress;
        private String projectStatus;
        private Double distanceKm;
    }
}
