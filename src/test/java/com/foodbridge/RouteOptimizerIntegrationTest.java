package com.foodbridge;

import com.foodbridge.dto.AuthResponse;
import com.foodbridge.dto.LoginRequest;
import com.foodbridge.dto.OptimizedRouteDTO;
import com.foodbridge.dto.RegisterRequest;
import com.foodbridge.dto.RouteRequestDTO;
import com.foodbridge.entity.FoodListing;
import com.foodbridge.entity.FoodListing.FoodCondition;
import com.foodbridge.entity.FoodListing.FoodType;
import com.foodbridge.entity.User.Role;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
    "spring.profiles.active=local"
})
class RouteOptimizerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void testVolunteerRouteOptimization() {
        // 1. Register Donor
        RegisterRequest donorReg = new RegisterRequest(
                "Route Donor",
                "donor_route@foodbridge.com",
                "pwd123",
                Role.DONOR,
                "999991",
                "Chennai",
                13.0827,
                80.2707
        );
        restTemplate.postForEntity("/api/auth/register", donorReg, String.class);

        // 2. Register NGO (Base location at 13.0, 80.0)
        RegisterRequest ngoReg = new RegisterRequest(
                "Route NGO",
                "ngo_route@foodbridge.com",
                "pwd123",
                Role.NGO,
                "999992",
                "Chennai",
                13.0,
                80.0
        );
        restTemplate.postForEntity("/api/auth/register", ngoReg, String.class);

        // 3. Login Donor & get token
        LoginRequest donorLogin = new LoginRequest("donor_route@foodbridge.com", "pwd123");
        ResponseEntity<AuthResponse> donorLoginRes = restTemplate.postForEntity("/api/auth/login", donorLogin, AuthResponse.class);
        String donorToken = donorLoginRes.getBody().getToken();

        // 4. Login NGO & get token
        LoginRequest ngoLogin = new LoginRequest("ngo_route@foodbridge.com", "pwd123");
        ResponseEntity<AuthResponse> ngoLoginRes = restTemplate.postForEntity("/api/auth/login", ngoLogin, AuthResponse.class);
        String ngoToken = ngoLoginRes.getBody().getToken();

        // 5. Create 3 food listings as DONOR at varying distances from NGO (13.0, 80.0)
        // Listing 1: nearest (13.01, 80.01)
        FoodListing listing1 = FoodListing.builder()
                .foodName("Nearest Meals")
                .quantity("10 Packets")
                .foodType(FoodType.COOKED_MEAL)
                .condition(FoodCondition.FRESH)
                .location("Loc 1")
                .latitude(13.01)
                .longitude(80.01)
                .pickupMinutes(60)
                .build();

        // Listing 2: furthest (13.05, 80.05)
        FoodListing listing2 = FoodListing.builder()
                .foodName("Furthest Meals")
                .quantity("15 Packets")
                .foodType(FoodType.COOKED_MEAL)
                .condition(FoodCondition.FRESH)
                .location("Loc 2")
                .latitude(13.05)
                .longitude(80.05)
                .pickupMinutes(60)
                .build();

        // Listing 3: middle (13.02, 80.02)
        FoodListing listing3 = FoodListing.builder()
                .foodName("Middle Meals")
                .quantity("20 Packets")
                .foodType(FoodType.COOKED_MEAL)
                .condition(FoodCondition.FRESH)
                .location("Loc 3")
                .latitude(13.02)
                .longitude(80.02)
                .pickupMinutes(60)
                .build();

        HttpHeaders donorHeaders = new HttpHeaders();
        donorHeaders.setBearerAuth(donorToken);

        ResponseEntity<FoodListing> res1 = restTemplate.postForEntity("/api/donor/listings", new HttpEntity<>(listing1, donorHeaders), FoodListing.class);
        ResponseEntity<FoodListing> res2 = restTemplate.postForEntity("/api/donor/listings", new HttpEntity<>(listing2, donorHeaders), FoodListing.class);
        ResponseEntity<FoodListing> res3 = restTemplate.postForEntity("/api/donor/listings", new HttpEntity<>(listing3, donorHeaders), FoodListing.class);

        Long id1 = res1.getBody().getId();
        Long id2 = res2.getBody().getId();
        Long id3 = res3.getBody().getId();

        // 6. Request Route Optimization as NGO for all 3 listings
        HttpHeaders ngoHeaders = new HttpHeaders();
        ngoHeaders.setBearerAuth(ngoToken);

        RouteRequestDTO routeRequest = new RouteRequestDTO(Arrays.asList(id1, id2, id3));

        ResponseEntity<OptimizedRouteDTO> routeRes = restTemplate.exchange(
                "/api/ngo/route/optimize",
                HttpMethod.POST,
                new HttpEntity<>(routeRequest, ngoHeaders),
                OptimizedRouteDTO.class
        );

        Assertions.assertTrue(routeRes.getStatusCode().is2xxSuccessful(), "Expected 200 OK for route optimization");
        OptimizedRouteDTO optimized = routeRes.getBody();
        Assertions.assertNotNull(optimized, "OptimizedRouteDTO should not be null");
        Assertions.assertEquals(3, optimized.getStops().size(), "Should have exactly 3 stops");

        // Verify Nearest Neighbour ordering: Nearest (id1) -> Middle (id3) -> Furthest (id2)
        Assertions.assertEquals(id1, optimized.getStops().get(0).getListingId(), "First stop should be nearest");
        Assertions.assertEquals(id3, optimized.getStops().get(1).getListingId(), "Second stop should be middle");
        Assertions.assertEquals(id2, optimized.getStops().get(2).getListingId(), "Third stop should be furthest");

        Assertions.assertTrue(optimized.getTotalDistanceKm() > 0, "Total distance should be greater than 0");
        Assertions.assertTrue(optimized.getEstimatedTotalMinutes() > 0, "Total minutes should be greater than 0");
        Assertions.assertTrue(optimized.getGoogleMapsUrl().contains("https://www.google.com/maps/dir/?api=1"), "Should contain valid Google Maps base URL");
        Assertions.assertTrue(optimized.getGoogleMapsUrl().contains("&origin=13.0,80.0"), "Should contain correct origin");
        Assertions.assertTrue(optimized.getGoogleMapsUrl().contains("&waypoints=13.01,80.01|13.02,80.02"), "Should contain correct waypoints");
        Assertions.assertTrue(optimized.getGoogleMapsUrl().contains("&destination=13.05,80.05"), "Should contain correct destination");
    }
}
