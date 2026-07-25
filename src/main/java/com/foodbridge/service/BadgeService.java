package com.foodbridge.service;

import com.foodbridge.entity.Badge;
import com.foodbridge.entity.Claim;
import com.foodbridge.entity.User;
import com.foodbridge.entity.User.Role;
import com.foodbridge.repository.BadgeRepository;
import com.foodbridge.repository.ClaimRepository;
import com.foodbridge.repository.FoodListingRepository;
import com.foodbridge.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;

@Service
@Transactional
public class BadgeService {

    @Autowired
    private BadgeRepository badgeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClaimRepository claimRepository;

    @Autowired
    private FoodListingRepository foodListingRepository;

    public void checkAndAwardBadges(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        if (user.getRole() == Role.NGO) {
            List<Claim> claims = claimRepository.findByNgoId(userId);
            long claimCount = claims.size();

            // Rule: "First rescue" -> first claim by this NGO
            if (claimCount >= 1) {
                awardBadge(user, "First rescue", "Rescued excess food for the first time!");
            }

            // Rule: "Speed claimer" -> claimed within 5 min of posting
            boolean hasSpeedClaim = false;
            for (Claim claim : claims) {
                if (claim.getListing() != null && claim.getListing().getCreatedAt() != null && claim.getClaimedAt() != null) {
                    Duration duration = Duration.between(claim.getListing().getCreatedAt(), claim.getClaimedAt());
                    if (duration.toMinutes() <= 5) {
                        hasSpeedClaim = true;
                        break;
                    }
                }
            }
            if (hasSpeedClaim) {
                awardBadge(user, "Speed claimer", "Claimed a donation within 5 minutes of publishing!");
            }

            // Calculate total meals
            int totalMeals = 0;
            for (Claim claim : claims) {
                if (claim.getListing() != null) {
                    totalMeals += parseQuantity(claim.getListing().getQuantity());
                }
            }

            // Rule: "10 meals hero" -> total meals >= 10
            if (totalMeals >= 10) {
                awardBadge(user, "10 meals hero", "Delivered 10 or more meals to hunger spots!");
            }

            // Rule: "100 meals legend" -> total meals >= 100
            if (totalMeals >= 100) {
                awardBadge(user, "100 meals legend", "Delivered 100 or more meals to hunger spots!");
            }

        } else if (user.getRole() == Role.DONOR) {
            // Rule: "Zero waste donor" -> donor with 100% claim rate
            long totalListings = foodListingRepository.countByDonorId(userId);
            long claimedListings = foodListingRepository.countByDonorIdAndStatus(userId, com.foodbridge.entity.FoodListing.ListingStatus.CLAIMED);

            if (totalListings > 0 && totalListings == claimedListings) {
                awardBadge(user, "Zero waste donor", "Maintained a 100% claim rate on all food listings!");
            }
        }
    }

    private void awardBadge(User user, String badgeName, String description) {
        if (!badgeRepository.existsByUserIdAndBadgeName(user.getId(), badgeName)) {
            Badge badge = Badge.builder()
                    .user(user)
                    .badgeName(badgeName)
                    .description(description)
                    .build();
            badgeRepository.save(badge);
        }
    }

    private int parseQuantity(String quantityStr) {
        if (quantityStr == null) return 0;
        try {
            String clean = quantityStr.replaceAll("[^0-9]", "");
            if (!clean.isEmpty()) {
                return Integer.parseInt(clean);
            }
        } catch (Exception e) {
            // fallback
        }
        return 10; // default meals value if parsing fails
    }
}
