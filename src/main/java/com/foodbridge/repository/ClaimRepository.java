package com.foodbridge.repository;

import com.foodbridge.entity.Claim;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClaimRepository extends JpaRepository<Claim, Long> {
    List<Claim> findByNgoId(Long ngoId);
    List<Claim> findByListingId(Long listingId);
    long countByNgoId(Long ngoId);
}
