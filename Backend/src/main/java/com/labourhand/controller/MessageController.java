package com.labourhand.controller;

import com.labourhand.dto.MessageDto;
import com.labourhand.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @GetMapping
    public ResponseEntity<List<MessageDto.ConversationResponse>> getConversations() {
        return ResponseEntity.ok(messageService.getMyConversations());
    }

    @GetMapping("/{conversationId}")
    public ResponseEntity<List<MessageDto.MessageResponse>> getMessages(
            @PathVariable Long conversationId) {
        return ResponseEntity.ok(messageService.getMessages(conversationId));
    }

    @PostMapping
    public ResponseEntity<MessageDto.MessageResponse> sendMessage(
            @Valid @RequestBody MessageDto.SendRequest req) {
        return ResponseEntity.ok(messageService.sendMessage(req));
    }

    @DeleteMapping("/{messageId}")
    public ResponseEntity<Void> deleteMessage(@PathVariable Long messageId) {
        messageService.deleteMessage(messageId);
        return ResponseEntity.noContent().build();
    }
}
