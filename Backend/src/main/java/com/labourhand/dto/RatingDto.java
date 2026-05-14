package com.labourhand.dto;

import lombok.Data;
import jakarta.validation.constraints.*;

public class RatingDto {

    @Data
    public static class Request {
        @NotNull
        private Long projectId;
        @NotNull
        private Long toUserId;
        @Min(1) @Max(5)
        private double rating;
        private String comment;
    }

    @Data
    public static class Response {
        private Long id;
        private Long projectId;
        private Long fromUserId;
        private String fromUserName;
        private Long toUserId;
        private double rating;
        private String comment;
        private String createdAt;
    }
}
