package com.foodbridge.controller;

import com.foodbridge.dto.RatingDTO;
import com.foodbridge.entity.Claim;
import com.foodbridge.entity.Rating;
import com.foodbridge.entity.User;
import com.foodbridge.repository.ClaimRepository;
import com.foodbridge.repository.RatingRepository;
import com.foodbridge.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/ratings")
public class RatingController {

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private ClaimRepository claimRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping
    public ResponseEntity<?> submitRating(@RequestBody RatingDTO request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<User> raterOpt = userRepository.findByEmail(email);
        if (raterOpt.isEmpty()) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        User rater = raterOpt.get();

        List<Claim> claims = claimRepository.findByListingId(request.getListingId());
        if (claims.isEmpty()) {
            return ResponseEntity.badRequest().body("No claims found for this listing");
        }
        
        // Use the most recent claim
        Claim claim = claims.get(claims.size() - 1);

        if (claim.getStatus() != Claim.ClaimStatus.PICKED_UP && claim.getStatus() != Claim.ClaimStatus.CLAIMED) {
            return ResponseEntity.badRequest().body("Claim must be CLAIMED or PICKED_UP to rate");
        }

        // Determine who is being rated
        User rated;
        if (rater.getId().equals(claim.getListing().getDonor().getId())) {
            // Rater is donor, so they rate the NGO
            rated = claim.getNgo();
        } else if (rater.getId().equals(claim.getNgo().getId())) {
            // Rater is NGO, so they rate the Donor
            rated = claim.getListing().getDonor();
        } else {
            return ResponseEntity.status(403).body("You are not part of this claim");
        }

        // Check for existing rating from this rater for this claim
        Optional<Rating> existing = ratingRepository.findByClaimIdAndRaterId(claim.getId(), rater.getId());
        if (existing.isPresent()) {
            return ResponseEntity.badRequest().body("You have already rated this claim");
        }

        if (request.getScore() < 1 || request.getScore() > 5) {
            return ResponseEntity.badRequest().body("Score must be between 1 and 5");
        }

        Rating rating = new Rating(claim, rater, rated, request.getScore(), request.getComment());
        ratingRepository.save(rating);

        return ResponseEntity.ok("Rating submitted successfully");
    }

    @GetMapping("/user/{userId}/average")
    public ResponseEntity<Double> getUserAverageRating(@PathVariable Long userId) {
        List<Rating> ratings = ratingRepository.findByRatedId(userId);
        if (ratings.isEmpty()) {
            return ResponseEntity.ok(0.0);
        }
        double sum = 0;
        for (Rating r : ratings) {
            sum += r.getScore();
        }
        return ResponseEntity.ok(sum / ratings.size());
    }

    @GetMapping("/listing/{listingId}")
    public ResponseEntity<List<Rating>> getRatingsForListing(@PathVariable Long listingId) {
        List<Claim> claims = claimRepository.findByListingId(listingId);
        if (claims.isEmpty()) {
            return ResponseEntity.ok(java.util.Collections.emptyList());
        }
        Claim claim = claims.get(claims.size() - 1);
        return ResponseEntity.ok(ratingRepository.findByClaimId(claim.getId()));
    }
}
