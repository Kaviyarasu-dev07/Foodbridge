package com.foodbridge;

import com.foodbridge.dto.AuthResponse;
import com.foodbridge.dto.FoodAlertDTO;
import com.foodbridge.dto.LoginRequest;
import com.foodbridge.dto.RegisterRequest;
import com.foodbridge.entity.Claim;
import com.foodbridge.entity.FoodListing;
import com.foodbridge.entity.FoodListing.FoodCondition;
import com.foodbridge.entity.FoodListing.FoodType;
import com.foodbridge.entity.FoodListing.ListingStatus;
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
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
    "spring.profiles.active=local"
})
class FoodListingIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void testCreateClaimAndWebSocketAlerts() throws Exception {
        // 1. Register Donor
        RegisterRequest donorReg = new RegisterRequest(
                "Test Donor",
                "donor_test@foodbridge.com",
                "pwd123",
                Role.DONOR,
                "111111",
                "Chennai",
                13.0827,
                80.2707
        );
        restTemplate.postForEntity("/api/auth/register", donorReg, String.class);

        // 2. Register NGO
        RegisterRequest ngoReg = new RegisterRequest(
                "Test NGO",
                "ngo_test@foodbridge.com",
                "pwd123",
                Role.NGO,
                "222222",
                "Chennai",
                13.0827,
                80.2707
        );
        restTemplate.postForEntity("/api/auth/register", ngoReg, String.class);

        // 3. Login Donor & get token
        LoginRequest donorLogin = new LoginRequest("donor_test@foodbridge.com", "pwd123");
        ResponseEntity<AuthResponse> donorLoginRes = restTemplate.postForEntity("/api/auth/login", donorLogin, AuthResponse.class);
        String donorToken = donorLoginRes.getBody().getToken();

        // 4. Login NGO & get token
        LoginRequest ngoLogin = new LoginRequest("ngo_test@foodbridge.com", "pwd123");
        ResponseEntity<AuthResponse> ngoLoginRes = restTemplate.postForEntity("/api/auth/login", ngoLogin, AuthResponse.class);
        String ngoToken = ngoLoginRes.getBody().getToken();

        // 5. Connect WebSocket Client to /ws and subscribe to /topic/food-alerts
        java.util.List<org.springframework.web.socket.sockjs.client.Transport> transports = new java.util.ArrayList<>();
        transports.add(new org.springframework.web.socket.sockjs.client.WebSocketTransport(new StandardWebSocketClient()));
        org.springframework.web.socket.sockjs.client.SockJsClient sockJsClient = new org.springframework.web.socket.sockjs.client.SockJsClient(transports);

        WebSocketStompClient stompClient = new WebSocketStompClient(sockJsClient);
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        BlockingQueue<FoodAlertDTO> alertQueue = new LinkedBlockingQueue<>();

        StompSession stompSession = stompClient.connect(
                "http://localhost:" + port + "/ws",
                new StompSessionHandlerAdapter() {}
        ).get(5, TimeUnit.SECONDS);

        stompSession.subscribe("/topic/food-alerts", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return FoodAlertDTO.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                alertQueue.offer((FoodAlertDTO) payload);
            }
        });

        // Give subscription a moment to complete handshake on the broker
        Thread.sleep(1000);

        // 6. Create food listing as DONOR
        FoodListing listing = FoodListing.builder()
                .foodName("Biryani Packets")
                .quantity("10 Packets")
                .foodType(FoodType.COOKED_MEAL)
                .condition(FoodCondition.FRESH)
                .location("Central Station, Chennai")
                .latitude(13.0827)
                .longitude(80.2707)
                .pickupMinutes(60)
                .build();

        HttpHeaders donorHeaders = new HttpHeaders();
        donorHeaders.setBearerAuth(donorToken);
        HttpEntity<FoodListing> donorEntity = new HttpEntity<>(listing, donorHeaders);

        ResponseEntity<FoodListing> listingRes = restTemplate.postForEntity(
                "/api/donor/listings",
                donorEntity,
                FoodListing.class
        );

        FoodListing createdListing = listingRes.getBody();
        Assertions.assertNotNull(createdListing);
        Assertions.assertEquals(ListingStatus.ACTIVE, createdListing.getStatus());
        Assertions.assertNotNull(createdListing.getQrCode());

        // 7. Verify WebSocket sends alert
        FoodAlertDTO alert = alertQueue.poll(10, TimeUnit.SECONDS);
        Assertions.assertNotNull(alert, "WebSocket alert was not received!");
        Assertions.assertEquals("Biryani Packets", alert.getFoodName());
        Assertions.assertEquals("10 Packets", alert.getQuantity());

        // 8. Claim Listing as NGO
        HttpHeaders ngoHeaders = new HttpHeaders();
        ngoHeaders.setBearerAuth(ngoToken);
        HttpEntity<Void> ngoEntity = new HttpEntity<>(ngoHeaders);

        ResponseEntity<Claim> claimRes = restTemplate.exchange(
                "/api/ngo/listings/" + createdListing.getId() + "/claim",
                HttpMethod.PUT,
                ngoEntity,
                Claim.class
        );

        Claim claim = claimRes.getBody();
        Assertions.assertNotNull(claim);
        Assertions.assertEquals(Claim.ClaimStatus.CLAIMED, claim.getStatus());
        Assertions.assertEquals(ListingStatus.CLAIMED, claim.getListing().getStatus());

        // 9. Verify NGO receives badges
        ResponseEntity<java.util.List> ngoBadgesRes = restTemplate.exchange(
                "/api/users/" + ngoLoginRes.getBody().getUserId() + "/badges",
                HttpMethod.GET,
                ngoEntity,
                java.util.List.class
        );
        Assertions.assertNotNull(ngoBadgesRes.getBody());
        Assertions.assertFalse(ngoBadgesRes.getBody().isEmpty());

        // 10. Verify NGO receives impact statistics
        ResponseEntity<java.util.Map> ngoImpactRes = restTemplate.exchange(
                "/api/users/" + ngoLoginRes.getBody().getUserId() + "/impact",
                HttpMethod.GET,
                ngoEntity,
                java.util.Map.class
        );
        Assertions.assertNotNull(ngoImpactRes.getBody());
        Assertions.assertEquals(10, ngoImpactRes.getBody().get("totalMeals"));
        Assertions.assertEquals(5.0, ngoImpactRes.getBody().get("co2SavedKg"));

        // 11. Verify Leaderboard reflects updates
        ResponseEntity<java.util.List> leaderboardNgosRes = restTemplate.exchange(
                "/api/leaderboard/ngos",
                HttpMethod.GET,
                ngoEntity,
                java.util.List.class
        );
        Assertions.assertNotNull(leaderboardNgosRes.getBody());
        Assertions.assertFalse(leaderboardNgosRes.getBody().isEmpty());
    }

    @Test
    void testAIEstimatePhotoAndValidations() throws Exception {
        // Register unique donor for this test method to avoid execution order issues
        RegisterRequest donorReg = new RegisterRequest(
                "AI Donor",
                "donor_ai@foodbridge.com",
                "pwd123",
                Role.DONOR,
                "98765",
                "Chennai",
                13.0827,
                80.2707
        );
        restTemplate.postForEntity("/api/auth/register", donorReg, String.class);

        LoginRequest donorLogin = new LoginRequest("donor_ai@foodbridge.com", "pwd123");
        ResponseEntity<AuthResponse> donorLoginRes = restTemplate.postForEntity("/api/auth/login", donorLogin, AuthResponse.class);
        String donorToken = donorLoginRes.getBody().getToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(donorToken);
        headers.setContentType(org.springframework.http.MediaType.MULTIPART_FORM_DATA);

        // 1. Valid Biryani Image (context mock check)
        org.springframework.util.MultiValueMap<String, Object> body = new org.springframework.util.LinkedMultiValueMap<>();
        org.springframework.core.io.ByteArrayResource fileResource = new org.springframework.core.io.ByteArrayResource("mockImageBytes".getBytes()) {
            @Override
            public String getFilename() {
                return "biryani.jpg";
            }
        };
        body.add("image", new HttpEntity<>(fileResource, createHeaders("image/jpeg")));
        HttpEntity<org.springframework.util.MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<com.foodbridge.dto.AIFoodEstimateDTO> response = restTemplate.postForEntity(
                "/api/donor/listings/estimate-photo",
                requestEntity,
                com.foodbridge.dto.AIFoodEstimateDTO.class
        );

        Assertions.assertEquals(200, response.getStatusCodeValue());
        Assertions.assertNotNull(response.getBody());
        Assertions.assertEquals("Biryani Packets", response.getBody().getFoodName());
        Assertions.assertEquals(12, response.getBody().getQuantity());
        Assertions.assertEquals("FRESH", response.getBody().getCondition());
        Assertions.assertEquals("COOKED_MEAL", response.getBody().getFoodType());

        // 2. Invalid File Type
        org.springframework.util.MultiValueMap<String, Object> bodyInvalidType = new org.springframework.util.LinkedMultiValueMap<>();
        org.springframework.core.io.ByteArrayResource textResource = new org.springframework.core.io.ByteArrayResource("plain text bytes".getBytes()) {
            @Override
            public String getFilename() {
                return "test.txt";
            }
        };
        bodyInvalidType.add("image", new HttpEntity<>(textResource, createHeaders("text/plain")));
        HttpEntity<org.springframework.util.MultiValueMap<String, Object>> requestInvalidType = new HttpEntity<>(bodyInvalidType, headers);

        ResponseEntity<java.util.Map> responseInvalidType = restTemplate.postForEntity(
                "/api/donor/listings/estimate-photo",
                requestInvalidType,
                java.util.Map.class
        );
        Assertions.assertEquals(400, responseInvalidType.getStatusCodeValue());
        Assertions.assertTrue(responseInvalidType.getBody().containsKey("error"));

        // 3. Large File size simulation (> 1MB)
        org.springframework.util.MultiValueMap<String, Object> bodyLarge = new org.springframework.util.LinkedMultiValueMap<>();
        byte[] largeBytes = new byte[2 * 1024 * 1024]; // 2MB
        org.springframework.core.io.ByteArrayResource largeResource = new org.springframework.core.io.ByteArrayResource(largeBytes) {
            @Override
            public String getFilename() {
                return "large_image.png";
            }
        };
        bodyLarge.add("image", new HttpEntity<>(largeResource, createHeaders("image/png")));
        HttpEntity<org.springframework.util.MultiValueMap<String, Object>> requestLarge = new HttpEntity<>(bodyLarge, headers);

        ResponseEntity<java.util.Map> responseLarge = restTemplate.postForEntity(
                "/api/donor/listings/estimate-photo",
                requestLarge,
                java.util.Map.class
        );
        Assertions.assertEquals(400, responseLarge.getStatusCodeValue());
        Assertions.assertTrue(responseLarge.getBody().get("error").toString().contains("exceeds"));
    }

    @Test
    void testPublicLiveImpactStats() {
        ResponseEntity<com.foodbridge.dto.LiveImpactDTO> initialRes = restTemplate.getForEntity(
                "/api/public/impact",
                com.foodbridge.dto.LiveImpactDTO.class
        );
        Assertions.assertEquals(200, initialRes.getStatusCodeValue());
        Assertions.assertNotNull(initialRes.getBody());

        ResponseEntity<java.util.Map> cityRes = restTemplate.getForEntity(
                "/api/public/impact/city",
                java.util.Map.class
        );
        Assertions.assertEquals(200, cityRes.getStatusCodeValue());
        Assertions.assertNotNull(cityRes.getBody());
    }

    private HttpHeaders createHeaders(String mimeType) {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(org.springframework.http.MediaType.parseMediaType(mimeType));
        return h;
    }
}
