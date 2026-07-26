package com.foodbridge.controller;

import com.foodbridge.entity.FoodListing;
import com.foodbridge.entity.FoodListing.ListingStatus;
import com.foodbridge.entity.User;
import com.foodbridge.entity.User.Role;
import com.foodbridge.entity.User.UserStatus;
import com.foodbridge.entity.VerificationStatus;
import com.foodbridge.entity.Claim;
import com.foodbridge.repository.FoodListingRepository;
import com.foodbridge.repository.UserRepository;
import com.foodbridge.repository.ClaimRepository;
import com.foodbridge.dto.PlatformAnalyticsDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FoodListingRepository foodListingRepository;

    @Autowired
    private ClaimRepository claimRepository;

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

    @GetMapping("/users/pending-verification")
    public ResponseEntity<List<User>> getPendingVerifications() {
        List<User> pendingUsers = userRepository.findAll().stream()
                .filter(u -> u.getVerificationStatus() == VerificationStatus.PENDING && u.getVerificationDocumentUrl() != null)
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(pendingUsers);
    }

    @PutMapping("/users/{id}/verify")
    public ResponseEntity<User> verifyUser(
            @PathVariable Long id,
            @RequestParam VerificationStatus status) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + id));
        user.setVerificationStatus(status);
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

    @GetMapping("/analytics")
    public ResponseEntity<PlatformAnalyticsDTO> getPlatformAnalytics() {
        List<FoodListing> allListings = foodListingRepository.findAll();
        List<Claim> allClaims = claimRepository.findAll();

        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
        Map<String, Integer> listingsPerDay = new TreeMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (int i = 0; i <= 30; i++) {
            listingsPerDay.put(thirtyDaysAgo.plusDays(i).format(formatter), 0);
        }

        long totalListings = allListings.size();
        long claimedListingsCount = 0;

        for (FoodListing listing : allListings) {
            if (listing.getCreatedAt() != null) {
                LocalDate createdDate = listing.getCreatedAt().toLocalDate();
                if (!createdDate.isBefore(thirtyDaysAgo)) {
                    String dateStr = createdDate.format(formatter);
                    listingsPerDay.put(dateStr, listingsPerDay.getOrDefault(dateStr, 0) + 1);
                }
            }
            if (listing.getStatus() == ListingStatus.CLAIMED) {
                claimedListingsCount++;
            }
        }

        double claimRatePercentage = totalListings > 0 ? ((double) claimedListingsCount / totalListings) * 100.0 : 0.0;

        long totalTimeToClaimMinutes = 0;
        int validClaims = 0;

        for (Claim claim : allClaims) {
            if (claim.getListing() != null && claim.getListing().getCreatedAt() != null && claim.getClaimedAt() != null) {
                long minutes = ChronoUnit.MINUTES.between(claim.getListing().getCreatedAt(), claim.getClaimedAt());
                if (minutes >= 0) {
                    totalTimeToClaimMinutes += minutes;
                    validClaims++;
                }
            }
        }

        double averageTimeToClaimMinutes = validClaims > 0 ? (double) totalTimeToClaimMinutes / validClaims : 0.0;

        PlatformAnalyticsDTO dto = new PlatformAnalyticsDTO(listingsPerDay, claimRatePercentage, averageTimeToClaimMinutes);
        return ResponseEntity.ok(dto);
    }
}
