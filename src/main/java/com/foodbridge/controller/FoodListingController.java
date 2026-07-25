package com.foodbridge.controller;

import com.foodbridge.entity.Claim;
import com.foodbridge.entity.FoodListing;
import com.foodbridge.entity.FoodSOS;
import com.foodbridge.entity.User;
import com.foodbridge.repository.UserRepository;
import com.foodbridge.service.FoodListingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class FoodListingController {

    @Autowired
    private FoodListingService foodListingService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private com.foodbridge.service.AIFoodEstimatorService aiFoodEstimatorService;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found"));
    }

    @PostMapping("/donor/listings")
    public ResponseEntity<FoodListing> createListing(@RequestBody FoodListing listing) {
        User currentUser = getCurrentUser();
        FoodListing created = foodListingService.createListing(listing, currentUser);
        return ResponseEntity.ok(created);
    }

    @GetMapping("/ngo/listings/nearby")
    public ResponseEntity<List<FoodListing>> getNearbyListings(
            @RequestParam double lat,
            @RequestParam double lng) {
        List<FoodListing> listings = foodListingService.getNearbyListings(lat, lng);
        return ResponseEntity.ok(listings);
    }

    @PutMapping("/ngo/listings/{id}/claim")
    public ResponseEntity<Claim> claimListing(@PathVariable Long id) {
        User currentUser = getCurrentUser();
        Claim claim = foodListingService.claimListing(id, currentUser);
        return ResponseEntity.ok(claim);
    }

    @GetMapping("/donor/listings/my")
    public ResponseEntity<List<FoodListing>> getMyListings() {
        User currentUser = getCurrentUser();
        List<FoodListing> listings = foodListingService.getMyListings(currentUser);
        return ResponseEntity.ok(listings);
    }

    @GetMapping("/admin/listings")
    public ResponseEntity<List<FoodListing>> getAllListings() {
        List<FoodListing> listings = foodListingService.getAllListings();
        return ResponseEntity.ok(listings);
    }

    @PostMapping("/ngo/listings/{id}/sos")
    public ResponseEntity<FoodSOS> triggerSOS(@PathVariable Long id) {
        FoodSOS sos = foodListingService.triggerSOS(id);
        return ResponseEntity.ok(sos);
    }

    @PostMapping("/donor/listings/estimate-photo")
    public ResponseEntity<?> estimatePhoto(@RequestParam("image") org.springframework.web.multipart.MultipartFile image) {
        if (image == null || image.isEmpty()) {
            return ResponseEntity.badRequest().body(java.util.Collections.singletonMap("error", "Image file is required."));
        }

        String contentType = image.getContentType();
        if (contentType == null || (!contentType.equals("image/jpeg") && !contentType.equals("image/png"))) {
            return ResponseEntity.badRequest().body(java.util.Collections.singletonMap("error", "Invalid file type. Only JPEG and PNG images are accepted."));
        }

        if (image.getSize() > 1 * 1024 * 1024) {
            return ResponseEntity.badRequest().body(java.util.Collections.singletonMap("error", "File size exceeds the limit of 1MB."));
        }

        try {
            com.foodbridge.dto.AIFoodEstimateDTO estimate = aiFoodEstimatorService.estimateFromPhoto(image);
            return ResponseEntity.ok(estimate);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(java.util.Collections.singletonMap("error", e.getMessage()));
        }
    }

    @Autowired
    private com.foodbridge.service.RouteOptimizerService routeOptimizerService;

    @PostMapping("/ngo/route/optimize")
    public ResponseEntity<com.foodbridge.dto.OptimizedRouteDTO> optimizeRoute(@RequestBody com.foodbridge.dto.RouteRequestDTO request) {
        User currentUser = getCurrentUser();
        com.foodbridge.dto.OptimizedRouteDTO optimized = routeOptimizerService.optimizeRoute(currentUser.getId(), request.getListingIds());
        return ResponseEntity.ok(optimized);
    }

    @Autowired
    private com.foodbridge.scheduler.PredictiveAlertScheduler predictiveAlertScheduler;

    @GetMapping("/ngo/alerts/predictive")
    public ResponseEntity<List<com.foodbridge.dto.PredictiveAlertPayload>> getPredictiveAlerts() {
        User currentUser = getCurrentUser();
        List<com.foodbridge.dto.PredictiveAlertPayload> alerts = predictiveAlertScheduler.getTodayPredictiveAlertsForNgo(currentUser);
        return ResponseEntity.ok(alerts);
    }
}
