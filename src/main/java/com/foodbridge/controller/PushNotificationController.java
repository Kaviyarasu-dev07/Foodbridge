package com.foodbridge.controller;

import com.foodbridge.dto.PushSubscriptionDTO;
import com.foodbridge.entity.User;
import com.foodbridge.repository.UserRepository;
import com.foodbridge.service.PushNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class PushNotificationController {

    @Autowired
    private PushNotificationService pushNotificationService;

    @Autowired
    private UserRepository userRepository;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found"));
    }

    @GetMapping("/public/push/vapid-public-key")
    public ResponseEntity<Map<String, String>> getPublicKey() {
        return ResponseEntity.ok(Collections.singletonMap("publicKey", pushNotificationService.getPublicKey()));
    }

    @PostMapping("/push/subscribe")
    public ResponseEntity<?> subscribe(@RequestBody PushSubscriptionDTO dto) {
        User currentUser = getCurrentUser();
        pushNotificationService.subscribe(currentUser, dto);
        return ResponseEntity.ok(Collections.singletonMap("message", "Subscribed successfully"));
    }

    @PostMapping("/push/unsubscribe")
    public ResponseEntity<?> unsubscribe(@RequestBody Map<String, String> payload) {
        String endpoint = payload.get("endpoint");
        if (endpoint != null) {
            pushNotificationService.unsubscribe(endpoint);
        }
        return ResponseEntity.ok(Collections.singletonMap("message", "Unsubscribed successfully"));
    }
}
