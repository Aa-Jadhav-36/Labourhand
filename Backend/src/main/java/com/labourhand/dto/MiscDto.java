package com.labourhand.dto;

import lombok.Data;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class MiscDto {

    @Data
    public static class ReviewRequest {
        @NotNull
        private Long projectId;
        @NotNull
        private Long revieweeId;
        @Min(1)
        @Max(5)
        private double rating;
        private String comment;
        private String duration;
    }

    @Data
    public static class ReviewResponse {
        private Long id;
        private Long projectId;
        private String projectName;
        private String clientName;
        private double rating;
        private String comment;
        private String duration;
        private String createdAt;
    }

    @Data
    public static class CertificationRequest {
        @NotBlank
        private String name;
        private String issuer;
        private String year;
    }

    @Data
    public static class CertificationResponse {
        private Long id;
        private String name;
        private String issuer;
        private String year;
    }

    @Data
    public static class SkillRequest {
        @NotBlank
        private String name;
    }

    @Data
    public static class ScheduleEventRequest {
        @NotNull
        private Long workerId;
        @NotNull
        private Long projectId;
        @NotNull
        private LocalDate date;
        private LocalTime startTime;
        private LocalTime endTime;
    }

    @Data
    public static class ScheduleEventResponse {
        private Long id;
        private Long workerId;
        private String workerName;
        private Long projectId;
        private String projectName;
        private String date;
        private String startTime;
        private String endTime;
    }

    @Data
    public static class EarningDataPoint {
        private String label; // "Mon", "Tue" or "Mar 1"
        private long amount;
    }

    @Data
    public static class EarningsResponse {
        private List<EarningDataPoint> data;
        private long total;
        private long average;
        private long peak;
    }

    @Data
    public static class ContractorProjectResponse {
        private Long id;
        private String name;
        private String location;
        private int progress;
        private int workersAssigned;
        private String deadline;
        private String status; // on-track, at-risk
    }
}
