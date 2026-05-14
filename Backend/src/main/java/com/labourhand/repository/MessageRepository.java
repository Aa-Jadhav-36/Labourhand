package com.labourhand.repository;

import com.labourhand.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByConversationIdOrderBySentAtAsc(Long conversationId);

    long countByConversationIdAndIsReadFalseAndSenderIdNot(Long conversationId, Long currentUserId);
}
