package com.foodbridge.repository;

import com.foodbridge.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByListingIdOrderBySentAtAsc(Long listingId);

    @Query("SELECT COUNT(c) FROM ChatMessage c WHERE c.listingId = :id AND c.senderRole = :role AND c.isRead = false")
    long countUnreadByListingIdAndSenderRole(@Param("id") Long id, @Param("role") String role);
}
