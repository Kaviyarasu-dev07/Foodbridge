package com.foodbridge.websocket;

import com.foodbridge.dto.ChatMessageDTO;
import com.foodbridge.entity.ChatMessage;
import com.foodbridge.entity.Claim;
import com.foodbridge.entity.FoodListing;
import com.foodbridge.entity.User;
import com.foodbridge.repository.ChatRepository;
import com.foodbridge.repository.ClaimRepository;
import com.foodbridge.repository.FoodListingRepository;
import com.foodbridge.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
public class ChatController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FoodListingRepository foodListingRepository;

    @Autowired
    private ClaimRepository claimRepository;

    @MessageMapping("/chat/{listingId}/send")
    public void sendMessage(@DestinationVariable("listingId") String listingIdStr, @Payload ChatMessageDTO messageDTO) {
        Long listingId = Long.parseLong(listingIdStr);
        processSendMessage(listingId, messageDTO);
    }

    @PostMapping("/api/chat/{listingId}/send")
    public ResponseEntity<?> sendMessageRest(@PathVariable Long listingId, @RequestBody ChatMessageDTO messageDTO) {
        ChatMessageDTO savedDTO = processSendMessage(listingId, messageDTO);
        return ResponseEntity.ok(savedDTO);
    }

    private ChatMessageDTO processSendMessage(Long listingId, ChatMessageDTO messageDTO) {
        ChatMessage message = ChatMessage.builder()
                .listingId(listingId)
                .senderId(messageDTO.getSenderId())
                .senderName(messageDTO.getSenderName())
                .senderRole(messageDTO.getSenderRole())
                .message(messageDTO.getMessage())
                .isRead(false)
                .sentAt(LocalDateTime.now())
                .build();
        
        ChatMessage saved = chatRepository.save(message);

        ChatMessageDTO savedDTO = ChatMessageDTO.builder()
                .id(saved.getId())
                .listingId(saved.getListingId())
                .senderId(saved.getSenderId())
                .senderName(saved.getSenderName())
                .senderRole(saved.getSenderRole())
                .message(saved.getMessage())
                .isRead(saved.getIsRead())
                .sentAt(saved.getSentAt() != null ? saved.getSentAt().toString() : null)
                .type("CHAT")
                .build();

        messagingTemplate.convertAndSend("/topic/chat/" + listingId, savedDTO);
        return savedDTO;
    }

    @MessageMapping("/chat/{listingId}/read")
    public void markMessagesAsRead(@DestinationVariable("listingId") String listingIdStr, @Payload Map<String, String> payload) {
        Long listingId = Long.parseLong(listingIdStr);
        processMarkMessagesAsRead(listingId, payload);
    }

    @PostMapping("/api/chat/{listingId}/read")
    public ResponseEntity<?> markMessagesAsReadRest(@PathVariable Long listingId, @RequestBody Map<String, String> payload) {
        processMarkMessagesAsRead(listingId, payload);
        return ResponseEntity.ok(Collections.singletonMap("success", true));
    }

    private void processMarkMessagesAsRead(Long listingId, Map<String, String> payload) {
        String readerRole = payload.get("role"); // DONOR or NGO
        if (readerRole == null) return;

        List<ChatMessage> messages = chatRepository.findByListingIdOrderBySentAtAsc(listingId);
        boolean updated = false;
        for (ChatMessage m : messages) {
            if (!m.getIsRead() && !m.getSenderRole().equalsIgnoreCase(readerRole)) {
                m.setIsRead(true);
                chatRepository.save(m);
                updated = true;
            }
        }

        if (updated) {
            ChatMessageDTO receipt = ChatMessageDTO.builder()
                    .listingId(listingId)
                    .type("READ_RECEIPT")
                    .build();
            messagingTemplate.convertAndSend("/topic/chat/" + listingId, receipt);
        }
    }

    @GetMapping("/api/chat/{listingId}/messages")
    public ResponseEntity<?> getChatHistory(@PathVariable Long listingId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (!userOpt.isPresent()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.singletonMap("error", "User not found"));
        }
        User currentUser = userOpt.get();

        Optional<FoodListing> listingOpt = foodListingRepository.findById(listingId);
        if (!listingOpt.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.singletonMap("error", "Listing not found"));
        }
        FoodListing listing = listingOpt.get();

        boolean isDonor = listing.getDonor().getId().equals(currentUser.getId());
        boolean isNgo = false;

        List<Claim> claims = claimRepository.findByListingId(listingId);
        for (Claim c : claims) {
            if (c.getNgo().getId().equals(currentUser.getId())) {
                isNgo = true;
                break;
            }
        }

        if (!isDonor && !isNgo && !currentUser.getRole().name().equalsIgnoreCase("ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Collections.singletonMap("error", "Access denied. Only donor or claiming NGO can access this chat."));
        }

        List<ChatMessage> messages = chatRepository.findByListingIdOrderBySentAtAsc(listingId);
        List<ChatMessageDTO> dtos = messages.stream().map(m -> ChatMessageDTO.builder()
                .id(m.getId())
                .listingId(m.getListingId())
                .senderId(m.getSenderId())
                .senderName(m.getSenderName())
                .senderRole(m.getSenderRole())
                .message(m.getMessage())
                .isRead(m.getIsRead())
                .sentAt(m.getSentAt() != null ? m.getSentAt().toString() : null)
                .type("CHAT")
                .build()).collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/api/chat/unread-count")
    public ResponseEntity<?> getUnreadCount() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (!userOpt.isPresent()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.singletonMap("error", "User not found"));
        }
        User currentUser = userOpt.get();

        long totalUnread = 0;
        Map<Long, Long> unreadByListing = new HashMap<>();

        if ("DONOR".equalsIgnoreCase(currentUser.getRole().name())) {
            List<FoodListing> listings = foodListingRepository.findByDonor(currentUser);
            for (FoodListing l : listings) {
                long count = chatRepository.countUnreadByListingIdAndSenderRole(l.getId(), "NGO");
                if (count > 0) {
                    totalUnread += count;
                    unreadByListing.put(l.getId(), count);
                }
            }
        } else if ("NGO".equalsIgnoreCase(currentUser.getRole().name())) {
            List<Claim> claims = claimRepository.findByNgoId(currentUser.getId());
            for (Claim c : claims) {
                long count = chatRepository.countUnreadByListingIdAndSenderRole(c.getListing().getId(), "DONOR");
                if (count > 0) {
                    totalUnread += count;
                    unreadByListing.put(c.getListing().getId(), count);
                }
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("total", totalUnread);
        response.put("byListing", unreadByListing);

        return ResponseEntity.ok(response);
    }
}
