package com.foodbridge.repository;

import com.foodbridge.entity.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {
    List<Rating> findByRatedId(Long ratedId);
    Optional<Rating> findByClaimIdAndRaterId(Long claimId, Long raterId);
    List<Rating> findByClaimId(Long claimId);
}
