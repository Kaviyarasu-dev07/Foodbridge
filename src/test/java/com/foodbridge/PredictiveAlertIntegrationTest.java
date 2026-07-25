package com.foodbridge;

import com.foodbridge.dto.AuthResponse;
import com.foodbridge.dto.LoginRequest;
import com.foodbridge.dto.PredictiveAlertPayload;
import com.foodbridge.dto.RegisterRequest;
import com.foodbridge.entity.FoodListing;
import com.foodbridge.entity.FoodListing.FoodCondition;
import com.foodbridge.entity.FoodListing.FoodType;
import com.foodbridge.entity.User.Role;
import com.foodbridge.scheduler.PredictiveAlertScheduler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
    "spring.profiles.active=local"
})
class PredictiveAlertIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private PredictiveAlertScheduler predictiveAlertScheduler;

    @Test
    void testPredictiveSurplusAlertSystem() {
        // 1. Register Donor
        RegisterRequest donorReg = new RegisterRequest(
                "Predictive Donor",
                "donor_predictive@foodbridge.com",
                "pwd123",
                Role.DONOR,
                "999995",
                "Chennai",
                13.0827,
                80.2707
        );
        restTemplate.postForEntity("/api/auth/register", donorReg, String.class);

        // 2. Register NGO (within 5km of donor, e.g. 13.0830, 80.2710)
        RegisterRequest ngoReg = new RegisterRequest(
                "Predictive NGO",
                "ngo_predictive@foodbridge.com",
                "pwd123",
                Role.NGO,
                "999996",
                "Chennai",
                13.0830,
                80.2710
        );
        restTemplate.postForEntity("/api/auth/register", ngoReg, String.class);

        // 3. Login Donor & get token
        LoginRequest donorLogin = new LoginRequest("donor_predictive@foodbridge.com", "pwd123");
        ResponseEntity<AuthResponse> donorLoginRes = restTemplate.postForEntity("/api/auth/login", donorLogin, AuthResponse.class);
        String donorToken = donorLoginRes.getBody().getToken();

        // 4. Login NGO & get token
        LoginRequest ngoLogin = new LoginRequest("ngo_predictive@foodbridge.com", "pwd123");
        ResponseEntity<AuthResponse> ngoLoginRes = restTemplate.postForEntity("/api/auth/login", ngoLogin, AuthResponse.class);
        String ngoToken = ngoLoginRes.getBody().getToken();

        // 5. Create 4 food listings as DONOR (to satisfy >= 3 threshold)
        HttpHeaders donorHeaders = new HttpHeaders();
        donorHeaders.setBearerAuth(donorToken);

        for (int i = 0; i < 4; i++) {
            FoodListing listing = FoodListing.builder()
                    .foodName("Biryani Surplus " + i)
                    .quantity("20 Packets")
                    .foodType(FoodType.COOKED_MEAL)
                    .condition(FoodCondition.FRESH)
                    .location("Chennai Food Hall")
                    .latitude(13.0827)
                    .longitude(80.2707)
                    .pickupMinutes(60)
                    .build();
            restTemplate.postForEntity("/api/donor/listings", new HttpEntity<>(listing, donorHeaders), FoodListing.class);
        }

        // 6. Simulate scheduled job running 1 hour before the target hour
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneHourBefore = now.minusHours(1);
        predictiveAlertScheduler.checkAndSendPredictiveAlerts(oneHourBefore);

        // 7. Request today's predictive alerts as NGO
        HttpHeaders ngoHeaders = new HttpHeaders();
        ngoHeaders.setBearerAuth(ngoToken);

        ResponseEntity<List<PredictiveAlertPayload>> alertsRes = restTemplate.exchange(
                "/api/ngo/alerts/predictive",
                HttpMethod.GET,
                new HttpEntity<>(ngoHeaders),
                new ParameterizedTypeReference<List<PredictiveAlertPayload>>() {}
        );

        Assertions.assertTrue(alertsRes.getStatusCode().is2xxSuccessful(), "Expected 200 OK for predictive alerts");
        List<PredictiveAlertPayload> alerts = alertsRes.getBody();
        Assertions.assertNotNull(alerts, "Alerts list should not be null");
        Assertions.assertFalse(alerts.isEmpty(), "Alerts list should contain at least one pattern matching today");

        PredictiveAlertPayload alert = alerts.get(0);
        Assertions.assertEquals("Predictive Donor", alert.getPattern().getDonorName(), "Donor name should match");
        Assertions.assertEquals("Chennai Food Hall", alert.getPattern().getDonorLocation(), "Donor location should match");
        Assertions.assertEquals(4, alert.getPattern().getOccurrences(), "Occurrences should be 4");
        Assertions.assertTrue(alert.getPattern().getConfidenceScore() > 0, "Confidence score should be calculated");
        Assertions.assertTrue(alert.getMessage().contains("usually posts"), "Message should contain predictive text");
        Assertions.assertTrue(alert.getDistanceKm() <= 5.0, "Distance should be within 5km");
    }
}
