package com.labourhand.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class WorkerPaymentDetailsDto {
    @NotBlank
    private String paymentMethod; // "BANK" or "UPI"
    
    // Bank details
    private String bankAccountNo;
    private String bankName;
    private String ifscCode;
    private String holderName;
    
    // UPI details
    private String upiId;
}
