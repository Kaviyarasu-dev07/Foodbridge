package com.foodbridge.service;

import com.foodbridge.entity.ImpactStats;
import com.foodbridge.entity.User;
import com.foodbridge.repository.ImpactStatsRepository;
import com.foodbridge.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class ImpactService {

    @Autowired
    private ImpactStatsRepository impactStatsRepository;

    @Autowired
    private UserRepository userRepository;

    public void updateImpactStats(Long userId, int quantity) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        String currentMonthYear = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));

        ImpactStats stats = impactStatsRepository.findByUserIdAndMonthYear(userId, currentMonthYear)
                .orElseGet(() -> ImpactStats.builder()
                        .user(user)
                        .monthYear(currentMonthYear)
                        .totalMeals(0)
                        .totalRescues(0)
                        .co2SavedKg(0.0)
                        .treesEquivalent(0)
                        .build());

        stats.setTotalRescues(stats.getTotalRescues() + 1);
        stats.setTotalMeals(stats.getTotalMeals() + quantity);
        stats.setCo2SavedKg(stats.getTotalMeals() * 0.5);
        stats.setTreesEquivalent((int) Math.round(stats.getCo2SavedKg() / 21.0));

        impactStatsRepository.save(stats);
    }

    public ImpactStats getMonthlyImpact(Long userId, String monthYear) {
        return impactStatsRepository.findByUserIdAndMonthYear(userId, monthYear).orElse(null);
    }

    // Cumulative stats across all months for certificate data
    public Map<String, Object> getCumulativeImpact(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        List<ImpactStats> allStats = impactStatsRepository.findByUserId(userId);
        
        int totalMeals = 0;
        int totalRescues = 0;
        double co2SavedKg = 0.0;
        int treesEquivalent = 0;

        for (ImpactStats stat : allStats) {
            totalMeals += stat.getTotalMeals();
            totalRescues += stat.getTotalRescues();
            co2SavedKg += stat.getCo2SavedKg();
        }
        treesEquivalent = (int) Math.round(co2SavedKg / 21.0);

        Map<String, Object> cumulative = new HashMap<>();
        cumulative.put("userName", user.getName());
        cumulative.put("userRole", user.getRole().name());
        cumulative.put("city", user.getCity());
        cumulative.put("totalMeals", totalMeals);
        cumulative.put("totalRescues", totalRescues);
        cumulative.put("co2SavedKg", co2SavedKg);
        cumulative.put("treesEquivalent", treesEquivalent);
        cumulative.put("trustScore", user.getTrustScore());
        
        return cumulative;
    }
}
