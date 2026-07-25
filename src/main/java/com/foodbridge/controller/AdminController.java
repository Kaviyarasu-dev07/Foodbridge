package com.foodbridge.controller;

import com.foodbridge.entity.FoodListing;
import com.foodbridge.entity.FoodListing.ListingStatus;
import com.foodbridge.entity.User;
import com.foodbridge.entity.User.Role;
import com.foodbridge.entity.User.UserStatus;
import com.foodbridge.repository.FoodListingRepository;
import com.foodbridge.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FoodListingRepository foodListingRepository;

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    @PutMapping("/users/{id}/status")
    public ResponseEntity<User> updateUserStatus(
            @PathVariable Long id,
            @RequestParam UserStatus status) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + id));
        user.setStatus(status);
        return ResponseEntity.ok(userRepository.save(user));
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getPlatformStats() {
        List<User> users = userRepository.findAll();
        List<FoodListing> listings = foodListingRepository.findAll();

        long ngosCount = users.stream().filter(u -> u.getRole() == Role.NGO).count();
        long donorsCount = users.stream().filter(u -> u.getRole() == Role.DONOR).count();
        long liveCount = listings.stream().filter(l -> l.getStatus() == ListingStatus.ACTIVE).count();
        long claimedCount = listings.stream().filter(l -> l.getStatus() == ListingStatus.CLAIMED).count();

        // Calculate total meals (parse quantity string where possible, or default to 10 per claimed listing)
        long totalMeals = 0;
        for (FoodListing listing : listings) {
            if (listing.getStatus() == ListingStatus.CLAIMED) {
                try {
                    // Try to extract number from quantity string e.g. "15 Packets" -> 15
                    String qtyStr = listing.getQuantity().replaceAll("[^0-9]", "");
                    if (!qtyStr.isEmpty()) {
                        totalMeals += Long.parseLong(qtyStr);
                    } else {
                        totalMeals += 10; // default fallback
                    }
                } catch (Exception e) {
                    totalMeals += 10;
                }
            }
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalMeals", totalMeals);
        stats.put("totalNgos", ngosCount);
        stats.put("totalDonors", donorsCount);
        stats.put("totalListings", listings.size());
        stats.put("liveListings", liveCount);

        return ResponseEntity.ok(stats);
    }
}
