package com.foodbridge.controller;

import com.foodbridge.entity.Badge;
import com.foodbridge.entity.ImpactStats;
import com.foodbridge.entity.User;
import com.foodbridge.entity.User.Role;
import com.foodbridge.repository.BadgeRepository;
import com.foodbridge.repository.ImpactStatsRepository;
import com.foodbridge.repository.UserRepository;
import com.foodbridge.service.ImpactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class LeaderboardController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ImpactStatsRepository impactStatsRepository;

    @Autowired
    private BadgeRepository badgeRepository;

    @Autowired
    private ImpactService impactService;

    @GetMapping("/leaderboard/donors")
    public ResponseEntity<?> getTopDonors() {
        String currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        List<User> donors = userRepository.findByRole(Role.DONOR);

        List<Map<String, Object>> leaderboard = donors.stream().map(donor -> {
            ImpactStats stats = impactStatsRepository.findByUserIdAndMonthYear(donor.getId(), currentMonth).orElse(null);
            int mealsCount = stats != null ? stats.getTotalMeals() : 0;
            
            List<Badge> badges = badgeRepository.findByUserId(donor.getId());
            String topBadge = badges.isEmpty() ? "None" : badges.get(badges.size() - 1).getBadgeName();

            Map<String, Object> entry = new HashMap<>();
            entry.put("userId", donor.getId());
            entry.put("name", donor.getName());
            entry.put("city", donor.getCity());
            entry.put("mealsCount", mealsCount);
            entry.put("topBadge", topBadge);
            return entry;
        })
        .sorted((a, b) -> Integer.compare((int) b.get("mealsCount"), (int) a.get("mealsCount")))
        .limit(10)
        .collect(Collectors.toList());

        // Assign ranks
        for (int i = 0; i < leaderboard.size(); i++) {
            leaderboard.get(i).put("rank", i + 1);
        }

        return ResponseEntity.ok(leaderboard);
    }

    @GetMapping("/leaderboard/ngos")
    public ResponseEntity<?> getTopNgos() {
        String currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        List<User> ngos = userRepository.findByRole(Role.NGO);

        List<Map<String, Object>> leaderboard = ngos.stream().map(ngo -> {
            ImpactStats stats = impactStatsRepository.findByUserIdAndMonthYear(ngo.getId(), currentMonth).orElse(null);
            int rescuesCount = stats != null ? stats.getTotalRescues() : 0;
            
            List<Badge> badges = badgeRepository.findByUserId(ngo.getId());
            String topBadge = badges.isEmpty() ? "None" : badges.get(badges.size() - 1).getBadgeName();

            Map<String, Object> entry = new HashMap<>();
            entry.put("userId", ngo.getId());
            entry.put("name", ngo.getName());
            entry.put("city", ngo.getCity());
            entry.put("rescuesCount", rescuesCount);
            entry.put("topBadge", topBadge);
            return entry;
        })
        .sorted((a, b) -> Integer.compare((int) b.get("rescuesCount"), (int) a.get("rescuesCount")))
        .limit(10)
        .collect(Collectors.toList());

        // Assign ranks
        for (int i = 0; i < leaderboard.size(); i++) {
            leaderboard.get(i).put("rank", i + 1);
        }

        return ResponseEntity.ok(leaderboard);
    }

    @GetMapping("/users/{id}/badges")
    public ResponseEntity<List<Badge>> getUserBadges(@PathVariable Long id) {
        return ResponseEntity.ok(badgeRepository.findByUserId(id));
    }

    @GetMapping("/users/{id}/impact")
    public ResponseEntity<?> getUserImpact(@PathVariable Long id) {
        try {
            Map<String, Object> cumulativeStats = impactService.getCumulativeImpact(id);
            return ResponseEntity.ok(cumulativeStats);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
