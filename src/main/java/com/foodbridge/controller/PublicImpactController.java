package com.foodbridge.controller;

import com.foodbridge.dto.LiveImpactDTO;
import com.foodbridge.service.ImpactCalculatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/public")
public class PublicImpactController {

    @Autowired
    private ImpactCalculatorService impactCalculatorService;

    @GetMapping("/impact")
    public ResponseEntity<LiveImpactDTO> getLiveImpact() {
        return ResponseEntity.ok(impactCalculatorService.getPlatformTotals());
    }

    @GetMapping("/impact/city")
    public ResponseEntity<Map<String, Integer>> getCityBreakdown() {
        return ResponseEntity.ok(impactCalculatorService.getCityBreakdown());
    }
}
