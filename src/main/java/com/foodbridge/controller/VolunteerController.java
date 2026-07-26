package com.foodbridge.controller;

import com.foodbridge.dto.OptimizedRouteDTO;
import com.foodbridge.dto.RouteRequestDTO;
import com.foodbridge.entity.DeliveryStatus;
import com.foodbridge.entity.FoodListing;
import com.foodbridge.entity.User;
import com.foodbridge.repository.FoodListingRepository;
import com.foodbridge.repository.UserRepository;
import com.foodbridge.service.RouteOptimizerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/volunteer")
public class VolunteerController {

    @Autowired
    private FoodListingRepository foodListingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RouteOptimizerService routeOptimizerService;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    @GetMapping("/listings/available")
    public ResponseEntity<List<FoodListing>> getAvailableDeliveries() {
        // Return listings that are CLAIMED but not yet assigned to any volunteer
        List<FoodListing> available = foodListingRepository.findAll().stream()
                .filter(l -> l.getStatus() == FoodListing.ListingStatus.CLAIMED && l.getVolunteer() == null)
                .collect(Collectors.toList());
        return ResponseEntity.ok(available);
    }

    @GetMapping("/listings/accepted")
    public ResponseEntity<List<FoodListing>> getAcceptedDeliveries() {
        User volunteer = getCurrentUser();
        // Return listings assigned to this volunteer that are IN_PROGRESS
        List<FoodListing> accepted = foodListingRepository.findAll().stream()
                .filter(l -> l.getVolunteer() != null && l.getVolunteer().getId().equals(volunteer.getId()) && l.getDeliveryStatus() == DeliveryStatus.IN_PROGRESS)
                .collect(Collectors.toList());
        return ResponseEntity.ok(accepted);
    }

    @PutMapping("/listings/{id}/accept")
    public ResponseEntity<FoodListing> acceptDelivery(@PathVariable Long id) {
        User volunteer = getCurrentUser();
        FoodListing listing = foodListingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Listing not found"));

        if (listing.getStatus() != FoodListing.ListingStatus.CLAIMED) {
            return ResponseEntity.badRequest().body(null);
        }
        if (listing.getVolunteer() != null) {
            return ResponseEntity.badRequest().body(null); // already accepted
        }

        listing.setVolunteer(volunteer);
        listing.setDeliveryStatus(DeliveryStatus.IN_PROGRESS);
        return ResponseEntity.ok(foodListingRepository.save(listing));
    }

    @PutMapping("/listings/{id}/complete")
    public ResponseEntity<FoodListing> completeDelivery(@PathVariable Long id) {
        User volunteer = getCurrentUser();
        FoodListing listing = foodListingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Listing not found"));

        if (listing.getVolunteer() == null || !listing.getVolunteer().getId().equals(volunteer.getId())) {
            return ResponseEntity.status(403).body(null);
        }

        listing.setDeliveryStatus(DeliveryStatus.COMPLETED);
        // Let's also set the listing status to something else if needed, but requirements say "mark delivery as completed"
        // so we just update the deliveryStatus
        return ResponseEntity.ok(foodListingRepository.save(listing));
    }

    @PostMapping("/route/optimize")
    public ResponseEntity<OptimizedRouteDTO> optimizeRoute(@RequestBody RouteRequestDTO request) {
        User volunteer = getCurrentUser();
        // Reuse the RouteOptimizerService by passing the volunteer's ID as the origin (the service uses user.getLatitude/Longitude)
        OptimizedRouteDTO optimized = routeOptimizerService.optimizeRoute(volunteer.getId(), request.getListingIds());
        return ResponseEntity.ok(optimized);
    }
}
