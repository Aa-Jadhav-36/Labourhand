package com.labourhand.dto;

import lombok.Data;
import java.time.LocalDateTime;

public class PaymentDto {

    @Data
    public static class Request {
        private Long progressUpdateId;
        private Double amount;
        private String paymentMethod; // "ONLINE" or "CASH"
        private String referenceId; // Razorpay payment_id or cash note
    }

    @Data
    public static class Response {
        private Long id;
        private Long projectId;
        private Long progressUpdateId;
        private Long payerId;
        private String payerName;
        private Long payeeId;
        private String payeeName;
        private Double amount;
        private String paymentMethod;
        private String status;
        private String referenceId;
        private String createdAt;
        private String paidAt;
    }
}
