package com.labourhand.dto;

import com.labourhand.model.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

public class AuthDto {

    @Data
    public static class LoginRequest {
        @NotBlank
        @Email
        private String email;
        @NotBlank
        @Size(min = 6)
        private String password;
    }

    @Data
    public static class RegisterRequest {
        @NotBlank
        private String name;
        @NotBlank
        @Email
        private String email;
        @NotBlank
        private String phone;
        @NotBlank
        @Size(min = 6)
        private String password;
        private User.Role role; // WORKER or OWNER
        private String companyName; // owner only
        private String specialization; // worker only
        private String language = "en";
    }

    @Data
    public static class AuthResponse {
        private String token;
        private UserDto user;
    }
}
