package com.foodbridge.service;

import com.foodbridge.dto.LiveImpactDTO;
import com.foodbridge.entity.Claim;
import com.foodbridge.entity.FoodListing;
import com.foodbridge.entity.User;
import com.foodbridge.repository.ClaimRepository;
import com.foodbridge.repository.FoodListingRepository;
import com.foodbridge.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ImpactCalculatorService {

    @Autowired
    private FoodListingRepository foodListingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClaimRepository claimRepository;

    // Based on EPA/published estimates: approximately 2.5 kg CO2-equivalent 
    // prevented per kg of food waste diverted from landfill.
    private static final double CO2_EQUIVALENT_PER_KG_FOOD = 2.5;

    public double calculateCO2EquivalentPrevented(double kgOfFoodRescued) {
        return kgOfFoodRescued * CO2_EQUIVALENT_PER_KG_FOOD;
    }

    public double calculateCO2Saved(int mealsRescued) {
        return mealsRescued * 0.5;
    }

    public double calculateTreesEquivalent(double co2Kg) {
        return co2Kg / 21.0;
    }

    public long calculateWaterSaved(int mealsRescued) {
        return (long) mealsRescued * 240;
    }

    public LiveImpactDTO getPlatformTotals() {
        List<FoodListing> claimed = foodListingRepository.findByStatus(FoodListing.ListingStatus.CLAIMED);
        
        long totalMeals = 0;
        for (FoodListing listing : claimed) {
            totalMeals += parseQuantity(listing.getQuantity());
        }

        double totalCO2 = calculateCO2Saved((int) totalMeals);
        double totalTrees = calculateTreesEquivalent(totalCO2);
        long totalWater = calculateWaterSaved((int) totalMeals);

        // Assume 1 meal = 0.5kg of food for the offset calculation
        double kgOfFoodRescued = totalMeals * 0.5;
        double co2EquivalentOffsetKg = calculateCO2EquivalentPrevented(kgOfFoodRescued);

        int todayMealsCount = getTodayMealsCount();
        int donorsCount = getActiveDonorsCount();
        int ngosCount = getActiveNGOsCount();

        return LiveImpactDTO.builder()
                .totalMealsRescued(totalMeals)
                .totalCO2SavedKg(totalCO2)
                .treesEquivalent(totalTrees)
                .waterSavedLitres(totalWater)
                .todayMeals(todayMealsCount)
                .activeDonors(donorsCount)
                .activeNGOs(ngosCount)
                .lastUpdated(LocalDateTime.now())
                .co2EquivalentOffsetKg(co2EquivalentOffsetKg)
                .build();
    }

    public Map<String, Integer> getCityBreakdown() {
        List<FoodListing> claimed = foodListingRepository.findByStatus(FoodListing.ListingStatus.CLAIMED);
        Map<String, Integer> breakdown = new HashMap<>();
        
        for (FoodListing listing : claimed) {
            String area = listing.getLocation();
            if (area != null) {
                int commaIndex = area.indexOf(',');
                if (commaIndex != -1) {
                    area = area.substring(0, commaIndex).trim();
                } else {
                    area = area.trim();
                }
            }
            if (area == null || area.isEmpty()) {
                area = "Central Chennai";
            }
            // Capitalize first letter for display consistency
            area = Character.toUpperCase(area.charAt(0)) + area.substring(1).toLowerCase();
            
            int quantity = parseQuantity(listing.getQuantity());
            breakdown.put(area, breakdown.getOrDefault(area, 0) + quantity);
        }
        return breakdown;
    }

    private int getTodayMealsCount() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);
        List<Claim> allClaims = claimRepository.findAll();
        int sum = 0;
        for (Claim claim : allClaims) {
            if (claim.getClaimedAt() != null && 
                !claim.getClaimedAt().isBefore(startOfDay) && 
                !claim.getClaimedAt().isAfter(endOfDay) &&
                claim.getStatus() != Claim.ClaimStatus.CANCELLED) {
                sum += parseQuantity(claim.getListing().getQuantity());
            }
        }
        return sum;
    }

    private int getActiveDonorsCount() {
        return (int) userRepository.findAll().stream()
                .filter(u -> u.getRole() == User.Role.DONOR && u.getStatus() == User.UserStatus.ACTIVE)
                .count();
    }

    private int getActiveNGOsCount() {
        return (int) userRepository.findAll().stream()
                .filter(u -> u.getRole() == User.Role.NGO && u.getStatus() == User.UserStatus.ACTIVE)
                .count();
    }

    private int parseQuantity(String quantityStr) {
        if (quantityStr == null) return 0;
        try {
            String clean = quantityStr.replaceAll("[^0-9]", "");
            if (!clean.isEmpty()) {
                return Integer.parseInt(clean);
            }
        } catch (Exception e) {
            // ignore
        }
        return 10;
    }
}
