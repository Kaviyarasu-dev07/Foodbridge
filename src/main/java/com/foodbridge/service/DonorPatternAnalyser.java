package com.foodbridge.service;

import com.foodbridge.dto.DonorPatternDTO;
import com.foodbridge.entity.FoodListing;
import com.foodbridge.entity.User;
import com.foodbridge.repository.FoodListingRepository;
import com.foodbridge.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class DonorPatternAnalyser {

    @Autowired
    private FoodListingRepository foodListingRepository;

    @Autowired
    private UserRepository userRepository;

    public List<DonorPatternDTO> analyseDonorPatterns(Long donorId) {
        User donor = userRepository.findById(donorId)
                .orElseThrow(() -> new IllegalArgumentException("Donor not found"));

        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        List<FoodListing> listings = foodListingRepository.findByDonorIdAndCreatedAtAfter(donorId, thirtyDaysAgo);

        // Group by dayOfWeek (1-7) and hourOfDay (0-23)
        // Key: "dayOfWeek_hourOfDay"
        Map<String, List<FoodListing>> grouped = new HashMap<>();

        for (FoodListing fl : listings) {
            if (fl.getCreatedAt() == null) continue;
            int dayOfWeek = fl.getCreatedAt().getDayOfWeek().getValue();
            int hourOfDay = fl.getCreatedAt().getHour();
            String key = dayOfWeek + "_" + hourOfDay;
            grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(fl);
        }

        List<DonorPatternDTO> patterns = new ArrayList<>();

        for (Map.Entry<String, List<FoodListing>> entry : grouped.entrySet()) {
            List<FoodListing> group = entry.getValue();
            if (group.size() >= 3) {
                String[] parts = entry.getKey().split("_");
                int dayOfWeek = Integer.parseInt(parts[0]);
                int hourOfDay = Integer.parseInt(parts[1]);
                String dayName = DayOfWeek.of(dayOfWeek).name();
                dayName = dayName.substring(0, 1).toUpperCase() + dayName.substring(1).toLowerCase();

                int totalQty = 0;
                Map<String, Integer> foodTypeCounts = new HashMap<>();
                String lastLocation = donor.getCity() != null ? donor.getCity() : "Chennai";
                Double lat = donor.getLatitude() != null ? donor.getLatitude() : 13.0827;
                Double lng = donor.getLongitude() != null ? donor.getLongitude() : 80.2707;

                for (FoodListing fl : group) {
                    int q = extractNumber(fl.getQuantity());
                    totalQty += q;

                    String ft = fl.getFoodType() != null ? fl.getFoodType().name() : "COOKED_MEAL";
                    foodTypeCounts.put(ft, foodTypeCounts.getOrDefault(ft, 0) + 1);

                    if (fl.getLocation() != null) lastLocation = fl.getLocation();
                    if (fl.getLatitude() != null) lat = fl.getLatitude();
                    if (fl.getLongitude() != null) lng = fl.getLongitude();
                }

                int avgQty = totalQty / group.size();
                if (avgQty == 0) avgQty = 10;

                String commonFoodType = "COOKED_MEAL";
                int maxCount = -1;
                for (Map.Entry<String, Integer> ftEntry : foodTypeCounts.entrySet()) {
                    if (ftEntry.getValue() > maxCount) {
                        maxCount = ftEntry.getValue();
                        commonFoodType = ftEntry.getKey();
                    }
                }

                int occurrences = group.size();
                double confidenceScore = (occurrences / 30.0) * 100.0;
                if (confidenceScore > 100.0) confidenceScore = 100.0;
                confidenceScore = Math.round(confidenceScore * 10.0) / 10.0;

                DonorPatternDTO pattern = new DonorPatternDTO(
                        donorId,
                        donor.getName(),
                        lastLocation,
                        dayOfWeek,
                        dayName,
                        hourOfDay,
                        avgQty,
                        commonFoodType,
                        occurrences,
                        confidenceScore,
                        lat,
                        lng
                );
                patterns.add(pattern);
            }
        }
        return patterns;
    }

    private int extractNumber(String s) {
        if (s == null) return 10;
        String digits = s.replaceAll("[^0-9]", "");
        if (digits.isEmpty()) return 10;
        try {
            return Integer.parseInt(digits);
        } catch (Exception e) {
            return 10;
        }
    }
}
