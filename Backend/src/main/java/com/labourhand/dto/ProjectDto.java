package com.labourhand.dto;

import com.labourhand.model.Project;
import lombok.Data;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

public class ProjectDto {

    @Data
    public static class Request {
        @NotBlank
        private String title;
        private String description;
        private String category;
        @Positive
        private long budget;
        @Positive
        private int timelineDays;
        @NotBlank
        private String location;
        private Double lat;
        private Double lng;
        private LocalDate deadline;
    }

    @Data
    public static class Response {
        private Long id;
        private String title;
        private String description;
        private String category;
        private long budget;
        private int timelineDays;
        private String location;
        private Double lat;
        private Double lng;
        private String status;
        private Long ownerId;
        private String ownerName;
        private int progress;
        private LocalDate deadline;
        private String postedAt;
        private int bidCount;
        private Double distanceKm; // only set in nearby searches
        
        // Accepted Bid details (populated if project is IN_PROGRESS or later)
        private Long workerId;
        private String acceptedBidWorkerName;
        private Long acceptedBidAmount;
        private Long acceptedBidPaid;
    }

    @Data
    public static class ProgressRequest {
        @Min(0)
        @Max(100)
        private int progress;
        private Project.Status status;
    }
}
