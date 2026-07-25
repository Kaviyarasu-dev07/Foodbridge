package com.foodbridge;

import com.foodbridge.dto.AuthResponse;
import com.foodbridge.dto.ChatMessageDTO;
import com.foodbridge.dto.LoginRequest;
import com.foodbridge.dto.RegisterRequest;
import com.foodbridge.entity.Claim;
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
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
    "spring.profiles.active=local"
})
class ChatIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void testRealTimeChatAndUnreadCounts() throws Exception {
        // 1. Register Donor
        RegisterRequest donorReg = new RegisterRequest(
                "Chat Donor",
                "donor_chat@foodbridge.com",
                "pwd123",
                Role.DONOR,
                "111112",
                "Chennai",
                13.0827,
                80.2707
        );
        restTemplate.postForEntity("/api/auth/register", donorReg, String.class);

        // 2. Register NGO
        RegisterRequest ngoReg = new RegisterRequest(
                "Chat NGO",
                "ngo_chat@foodbridge.com",
                "pwd123",
                Role.NGO,
                "222223",
                "Chennai",
                13.0827,
                80.2707
        );
        restTemplate.postForEntity("/api/auth/register", ngoReg, String.class);

        // 3. Login Donor & get token
        LoginRequest donorLogin = new LoginRequest("donor_chat@foodbridge.com", "pwd123");
        ResponseEntity<AuthResponse> donorLoginRes = restTemplate.postForEntity("/api/auth/login", donorLogin, AuthResponse.class);
        String donorToken = donorLoginRes.getBody().getToken();

        // 4. Login NGO & get token
        LoginRequest ngoLogin = new LoginRequest("ngo_chat@foodbridge.com", "pwd123");
        ResponseEntity<AuthResponse> ngoLoginRes = restTemplate.postForEntity("/api/auth/login", ngoLogin, AuthResponse.class);
        String ngoToken = ngoLoginRes.getBody().getToken();

        // 5. Create food listing as DONOR
        FoodListing listing = FoodListing.builder()
                .foodName("Chat Apples")
                .quantity("20 Kgs")
                .foodType(FoodType.RAW)
                .condition(FoodCondition.FRESH)
                .location("Chat Center")
                .latitude(13.0827)
                .longitude(80.2707)
                .pickupMinutes(60)
                .build();

        HttpHeaders donorHeaders = new HttpHeaders();
        donorHeaders.setBearerAuth(donorToken);
        HttpEntity<FoodListing> createReq = new HttpEntity<>(listing, donorHeaders);
        ResponseEntity<FoodListing> createRes = restTemplate.postForEntity("/api/donor/listings", createReq, FoodListing.class);
        Assertions.assertEquals(200, createRes.getStatusCodeValue());
        Long listingId = createRes.getBody().getId();

        // 6. Claim listing as NGO
        HttpHeaders ngoHeaders = new HttpHeaders();
        ngoHeaders.setBearerAuth(ngoToken);
        HttpEntity<Void> claimReq = new HttpEntity<>(null, ngoHeaders);
        ResponseEntity<Claim> claimRes = restTemplate.exchange("/api/ngo/listings/" + listingId + "/claim", HttpMethod.PUT, claimReq, Claim.class);
        Assertions.assertEquals(200, claimRes.getStatusCodeValue());
        Long ngoId = claimRes.getBody().getNgo().getId();

        // 7. Connect WebSocket Client to /ws and subscribe to /topic/chat/{listingId}
        java.util.List<org.springframework.web.socket.sockjs.client.Transport> transports = new java.util.ArrayList<>();
        transports.add(new org.springframework.web.socket.sockjs.client.WebSocketTransport(new StandardWebSocketClient()));
        org.springframework.web.socket.sockjs.client.SockJsClient sockJsClient = new org.springframework.web.socket.sockjs.client.SockJsClient(transports);

        WebSocketStompClient stompClient = new WebSocketStompClient(sockJsClient);
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        BlockingQueue<ChatMessageDTO> chatQueue = new LinkedBlockingQueue<>();

        StompSession stompSession = stompClient.connect(
                "http://localhost:" + port + "/ws",
                new StompSessionHandlerAdapter() {}
        ).get(5, TimeUnit.SECONDS);

        stompSession.subscribe("/topic/chat/" + listingId, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return ChatMessageDTO.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                chatQueue.offer((ChatMessageDTO) payload);
            }
        });

        Thread.sleep(2000);

        // 8. Send message via POST /api/chat/{listingId}/send
        ChatMessageDTO sentMsg = ChatMessageDTO.builder()
                .listingId(listingId)
                .senderId(ngoId)
                .senderName("Chat NGO")
                .senderRole("NGO")
                .message("On my way!")
                .build();
        
        HttpEntity<ChatMessageDTO> sendMsgReq = new HttpEntity<>(sentMsg, ngoHeaders);
        ResponseEntity<ChatMessageDTO> sendRes = restTemplate.postForEntity("/api/chat/" + listingId + "/send", sendMsgReq, ChatMessageDTO.class);
        Assertions.assertEquals(200, sendRes.getStatusCodeValue());

        // 9. Verify message received over WebSocket
        ChatMessageDTO receivedMsg = chatQueue.poll(5, TimeUnit.SECONDS);
        if (receivedMsg == null) {
            // Retry sending in case subscription took longer to register on the broker
            restTemplate.postForEntity("/api/chat/" + listingId + "/send", sendMsgReq, ChatMessageDTO.class);
            receivedMsg = chatQueue.poll(5, TimeUnit.SECONDS);
        }
        Assertions.assertNotNull(receivedMsg);
        Assertions.assertEquals("On my way!", receivedMsg.getMessage());
        Assertions.assertEquals("NGO", receivedMsg.getSenderRole());

        // 10. Verify GET /api/chat/{listingId}/messages returns chat history for donor
        HttpEntity<Void> getMsgReq = new HttpEntity<>(null, donorHeaders);
        ResponseEntity<List> historyRes = restTemplate.exchange("/api/chat/" + listingId + "/messages", HttpMethod.GET, getMsgReq, List.class);
        Assertions.assertEquals(200, historyRes.getStatusCodeValue());
        Assertions.assertFalse(historyRes.getBody().isEmpty());

        // 11. Verify GET /api/chat/unread-count for donor returns 1 unread message
        ResponseEntity<Map> unreadRes = restTemplate.exchange("/api/chat/unread-count", HttpMethod.GET, getMsgReq, Map.class);
        Assertions.assertEquals(200, unreadRes.getStatusCodeValue());
        Assertions.assertEquals(1, ((Number) unreadRes.getBody().get("total")).intValue());

        // 12. Send read receipt via POST /api/chat/{listingId}/read
        HttpEntity<Map<String, String>> readReq = new HttpEntity<>(java.util.Collections.singletonMap("role", "DONOR"), donorHeaders);
        restTemplate.postForEntity("/api/chat/" + listingId + "/read", readReq, Map.class);

        // Wait for read processing
        Thread.sleep(1000);

        // 13. Verify unread count is now 0
        ResponseEntity<Map> unreadResAfter = restTemplate.exchange("/api/chat/unread-count", HttpMethod.GET, getMsgReq, Map.class);
        Assertions.assertEquals(200, unreadResAfter.getStatusCodeValue());
        Assertions.assertEquals(0, ((Number) unreadResAfter.getBody().get("total")).intValue());
    }
}
