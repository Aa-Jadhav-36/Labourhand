package com.labourhand.dto;

import lombok.Data;
import java.time.LocalDateTime;

public class ProgressUpdateDto {

    @Data
    public static class Request {
        private Integer progressPercentage; // Optional if just commenting
        private String comment;
        private Boolean paymentDemanded;
        private Double demandAmount;
    }

    @Data
    public static class Response {
        private Long id;
        private Long projectId;
        private Long userId;
        private String userName; // For UI rendering
        private String userRole; // To distinguish between employer/worker side
        private String userAvatar; // For UI rendering
        private Integer progressPercentage;
        private String comment;
        private Boolean paymentDemanded;
        private Double demandAmount;
        private String paymentStatus; // e.g. "PENDING", "COMPLETED", "CASH_VERIFIED"
        private String createdAt;
    }
}
