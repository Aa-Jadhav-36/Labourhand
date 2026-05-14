package com.labourhand.service;

import com.labourhand.dto.MessageDto;
import com.labourhand.model.*;
import com.labourhand.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final UserService userService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * All conversations for the current user, enriched with last message + unread
     * count
     */
    public List<MessageDto.ConversationResponse> getMyConversations() {
        User me = userService.getCurrentUser();
        return conversationRepository.findByUserId(me.getId())
                .stream().map(c -> toConversationResponse(c, me.getId()))
                .collect(Collectors.toList());
    }

    public List<MessageDto.MessageResponse> getMessages(Long conversationId) {
        return messageRepository.findByConversationIdOrderBySentAtAsc(conversationId)
                .stream().map(this::toMessageResponse).collect(Collectors.toList());
    }

    @Transactional
    public MessageDto.MessageResponse sendMessage(MessageDto.SendRequest req) {
        User sender = userService.getCurrentUser();

        // Find or create conversation
        Conversation convo = conversationRepository
                .findByParticipants(sender.getId(), req.getReceiverId())
                .orElseGet(() -> {
                    Conversation c = Conversation.builder()
                            .participant1Id(sender.getId())
                            .participant2Id(req.getReceiverId())
                            .projectId(req.getProjectId())
                            .build();
                    return conversationRepository.save(c);
                });

        Message msg = Message.builder()
                .conversationId(convo.getId())
                .senderId(sender.getId())
                .content(req.getContent())
                .build();
        MessageDto.MessageResponse response = toMessageResponse(messageRepository.save(msg));

        // Broadcast to both receiver and sender over WebSockets
        messagingTemplate.convertAndSend("/topic/messages/" + req.getReceiverId(), response);
        messagingTemplate.convertAndSend("/topic/messages/" + sender.getId(), response);

        return response;
    }

    @Transactional
    public void deleteMessage(Long messageId) {
        messageRepository.deleteById(messageId);
    }

    // ── Mappers ────────────────────────────────────────────────────────────
    private MessageDto.ConversationResponse toConversationResponse(Conversation c, Long meId) {
        MessageDto.ConversationResponse r = new MessageDto.ConversationResponse();
        r.setConversationId(c.getId());

        Long otherId = c.getParticipant1Id().equals(meId) ? c.getParticipant2Id() : c.getParticipant1Id();
        r.setOtherUserId(otherId);
        userRepository.findById(otherId).ifPresent(u -> {
            r.setOtherUserName(u.getName());
            r.setOtherUserAvatar(u.getAvatar());
        });

        List<Message> msgs = messageRepository.findByConversationIdOrderBySentAtAsc(c.getId());
        if (!msgs.isEmpty()) {
            Message last = msgs.get(msgs.size() - 1);
            r.setLastMessage(last.getContent());
            r.setLastMessageTime(last.getSentAt() != null ? last.getSentAt().toString() : "");
        }

        r.setUnreadCount(messageRepository
                .countByConversationIdAndIsReadFalseAndSenderIdNot(c.getId(), meId));

        if (c.getProjectId() != null) {
            r.setProjectId(c.getProjectId());
            projectRepository.findById(c.getProjectId())
                    .ifPresent(p -> r.setProjectName(p.getTitle()));
        }
        return r;
    }

    private MessageDto.MessageResponse toMessageResponse(Message m) {
        MessageDto.MessageResponse r = new MessageDto.MessageResponse();
        r.setId(m.getId());
        r.setConversationId(m.getConversationId());
        r.setSenderId(m.getSenderId());
        r.setContent(m.getContent());
        r.setSentAt(m.getSentAt() != null ? m.getSentAt().toString() : "");
        r.setRead(m.isRead());
        return r;
    }
}
