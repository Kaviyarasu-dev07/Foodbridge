package com.foodbridge.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodbridge.dto.PushSubscriptionDTO;
import com.foodbridge.entity.PushSubscription;
import com.foodbridge.entity.User;
import com.foodbridge.repository.PushSubscriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class PushNotificationService {

    @Autowired
    private PushSubscriptionRepository pushSubscriptionRepository;

    private String publicKey;
    private String privateKey;

    @PostConstruct
    public void initVapidKeys() {
        try {
            // Run node push-sender.js generate
            ProcessBuilder pb = new ProcessBuilder("node", "push-sender.js", "generate");
            pb.directory(new File("."));
            Process process = pb.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line);
            }
            process.waitFor();

            ObjectMapper mapper = new ObjectMapper();
            Map keys = mapper.readValue(output.toString().trim(), Map.class);
            this.publicKey = (String) keys.get("publicKey");
            this.privateKey = (String) keys.get("privateKey");
            System.out.println("VAPID Keys loaded successfully. Public key: " + this.publicKey);
        } catch (Exception e) {
            System.err.println("Failed to initialize VAPID keys: " + e.getMessage());
            // Safe fallbacks for testing
            this.publicKey = "BEdA8Lh1Z6PjM4sBvP36bB3t481X0g14vC5d7a8K9x0z1e2r3t4y5u6i7o8p9a0s1d2f3g4h5j6k7l";
        }
    }

    public String getPublicKey() {
        return this.publicKey;
    }

    public PushSubscription subscribe(User user, PushSubscriptionDTO subDTO) {
        // Check if subscription already exists
        Optional<PushSubscription> existing = pushSubscriptionRepository.findByEndpoint(subDTO.getEndpoint());
        if (existing.isPresent()) {
            return existing.get();
        }

        PushSubscription sub = PushSubscription.builder()
                .user(user)
                .endpoint(subDTO.getEndpoint())
                .p256dh(subDTO.getP256dh())
                .auth(subDTO.getAuth())
                .createdAt(LocalDateTime.now())
                .build();

        return pushSubscriptionRepository.save(sub);
    }

    public void unsubscribe(String endpoint) {
        pushSubscriptionRepository.findByEndpoint(endpoint).ifPresent(sub -> {
            pushSubscriptionRepository.delete(sub);
        });
    }

    public void sendPushNotification(Long userId, String title, String body, String url) {
        List<PushSubscription> subs = pushSubscriptionRepository.findByUserId(userId);
        if (subs.isEmpty()) {
            return;
        }

        ObjectMapper mapper = new ObjectMapper();

        for (PushSubscription sub : subs) {
            try {
                // Prepare subscription JSON
                Map<String, Object> subMap = new HashMap<>();
                subMap.put("endpoint", sub.getEndpoint());
                
                Map<String, String> keysMap = new HashMap<>();
                keysMap.put("p256dh", sub.getP256dh());
                keysMap.put("auth", sub.getAuth());
                subMap.put("keys", keysMap);

                String subJson = mapper.writeValueAsString(subMap);

                // Prepare payload JSON
                Map<String, String> payloadMap = new HashMap<>();
                payloadMap.put("title", title);
                payloadMap.put("body", body);
                payloadMap.put("url", url);
                
                String payloadJson = mapper.writeValueAsString(payloadMap);

                // Spawn ProcessBuilder to send notification via Node helper
                ProcessBuilder pb = new ProcessBuilder("node", "push-sender.js", "send", subJson, payloadJson);
                pb.directory(new File("."));
                Process process = pb.start();

                BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                StringBuilder errorOutput = new StringBuilder();
                String errLine;
                while ((errLine = errorReader.readLine()) != null) {
                    errorOutput.append(errLine);
                }

                int exitCode = process.waitFor();
                if (exitCode != 0) {
                    System.err.println("Push sending failed for sub ID " + sub.getId() + ": " + errorOutput.toString());
                    
                    // Cleanup unsubscribed/expired endpoints (e.g. status code 410 Gone or 404 Not Found)
                    if (errorOutput.toString().contains("410") || errorOutput.toString().contains("404")) {
                        System.out.println("Cleaning up expired subscription ID " + sub.getId());
                        pushSubscriptionRepository.delete(sub);
                    }
                } else {
                    System.out.println("Push notification successfully delivered to sub ID " + sub.getId());
                }
            } catch (Exception e) {
                System.err.println("Error triggering process push for sub ID " + sub.getId() + ": " + e.getMessage());
            }
        }
    }
}
