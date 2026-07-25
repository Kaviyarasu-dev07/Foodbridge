package com.foodbridge.repository;

import com.foodbridge.entity.ImpactStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ImpactStatsRepository extends JpaRepository<ImpactStats, Long> {
    Optional<ImpactStats> findByUserIdAndMonthYear(Long userId, String monthYear);
    List<ImpactStats> findByUserId(Long userId);
}
