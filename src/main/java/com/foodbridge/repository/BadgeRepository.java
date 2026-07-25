package com.foodbridge.repository;

import com.foodbridge.entity.Badge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BadgeRepository extends JpaRepository<Badge, Long> {
    List<Badge> findByUserId(Long userId);
    boolean existsByUserIdAndBadgeName(Long userId, String badgeName);
}
