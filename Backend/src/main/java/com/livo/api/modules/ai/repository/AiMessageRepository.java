package com.livo.api.modules.ai.repository;

import com.livo.api.modules.ai.entity.AiMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AiMessageRepository extends JpaRepository<AiMessageEntity, UUID> {

    List<AiMessageEntity> findAllByConversationIdAndUserIdOrderByCreatedAtAsc(UUID conversationId, UUID userId);

    List<AiMessageEntity> findAllByConversationIdOrderByCreatedAtAsc(UUID conversationId);
}
