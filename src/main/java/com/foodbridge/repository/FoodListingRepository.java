package com.foodbridge.repository;

import com.foodbridge.entity.FoodListing;
import com.foodbridge.entity.FoodListing.ListingStatus;
import com.foodbridge.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FoodListingRepository extends JpaRepository<FoodListing, Long> {
    List<FoodListing> findByDonor(User donor);
    List<FoodListing> findByStatus(ListingStatus status);
    List<FoodListing> findByStatusAndExpiresAtBefore(ListingStatus status, LocalDateTime dateTime);
    long countByDonorId(Long donorId);
    long countByDonorIdAndStatus(Long donorId, ListingStatus status);
    List<FoodListing> findByDonorIdAndCreatedAtAfter(Long donorId, LocalDateTime date);
}
