package com.labourhand.dto;

import lombok.Data;
import jakarta.validation.constraints.*;

public class MessageDto {

    @Data
    public static class SendRequest {
        @NotNull
        private Long receiverId;
        @NotBlank
        private String content;
        private Long projectId; // optional context
    }

    @Data
    public static class ConversationResponse {
        private Long conversationId;
        private Long otherUserId;
        private String otherUserName;
        private String otherUserAvatar;
        private String lastMessage;
        private String lastMessageTime;
        private long unreadCount;
        private String projectName;
        private Long projectId;
    }

    @Data
    public static class MessageResponse {
        private Long id;
        private Long conversationId;
        private Long senderId;
        private String content;
        private String sentAt;
        private boolean isRead;
    }
}
