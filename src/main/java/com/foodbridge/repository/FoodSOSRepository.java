package com.foodbridge.repository;

import com.foodbridge.entity.FoodSOS;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FoodSOSRepository extends JpaRepository<FoodSOS, Long> {
}
