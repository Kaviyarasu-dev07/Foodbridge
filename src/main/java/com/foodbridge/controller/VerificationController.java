package com.foodbridge.controller;

import com.foodbridge.entity.User;
import com.foodbridge.entity.VerificationStatus;
import com.foodbridge.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

@RestController
@RequestMapping("/api")
public class VerificationController {

    @Autowired
    private UserRepository userRepository;

    // Authenticated upload for dashboard
    @PostMapping("/donor/verify/upload")
    public ResponseEntity<?> uploadVerificationDocument(@RequestParam("document") MultipartFile document) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return processUpload(document, currentUser);
    }

    // Public upload meant for immediately post-registration before login
    @PostMapping("/auth/{userId}/upload-verification")
    public ResponseEntity<?> uploadVerificationDocumentPublic(
            @PathVariable Long userId,
            @RequestParam("document") MultipartFile document) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Only allow if it's currently PENDING (preventing overwriting someone else's verified docs maliciously)
        if (user.getVerificationStatus() == VerificationStatus.VERIFIED) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "User is already verified."));
        }
        
        return processUpload(document, user);
    }

    private ResponseEntity<?> processUpload(MultipartFile document, User user) {
        if (document == null || document.isEmpty()) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Document is required."));
        }

        String contentType = document.getContentType();
        if (contentType == null || (!contentType.equals("image/jpeg") && !contentType.equals("image/png") && !contentType.equals("application/pdf"))) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Invalid file type. Only JPEG, PNG, and PDF are accepted."));
        }

        if (document.getSize() > 5 * 1024 * 1024) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "File size exceeds the limit of 5MB."));
        }

        try {
            String filename = System.currentTimeMillis() + "_" + document.getOriginalFilename().replaceAll("[^a-zA-Z0-9\\.\\-]", "_");
            Path uploadPath = Paths.get("uploads");
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Files.copy(document.getInputStream(), uploadPath.resolve(filename));

            user.setVerificationDocumentUrl("/uploads/" + filename);
            user.setVerificationStatus(VerificationStatus.PENDING);
            userRepository.save(user);

            return ResponseEntity.ok(Collections.singletonMap("message", "Document uploaded successfully. Status is pending review."));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Collections.singletonMap("error", "Failed to upload document: " + e.getMessage()));
        }
    }
}
