package com.foodbridge.service;

import com.foodbridge.dto.FoodAlertDTO;
import com.foodbridge.entity.FoodListing;
import com.foodbridge.entity.Notification;
import com.foodbridge.entity.Notification.NotificationType;
import com.foodbridge.entity.User;
import com.foodbridge.entity.User.Role;
import com.foodbridge.repository.NotificationRepository;
import com.foodbridge.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private ImpactCalculatorService impactCalculatorService;

    @Autowired
    private PushNotificationService pushNotificationService;

    public void sendFoodAlert(FoodListing listing) {
        if (listing.getLatitude() == null || listing.getLongitude() == null) {
            return;
        }

        List<User> ngos = userRepository.findByRole(Role.NGO);
        for (User ngo : ngos) {
            if (ngo.getLatitude() == null || ngo.getLongitude() == null) {
                continue;
            }

            double distance = calculateDistance(
                    listing.getLatitude(), listing.getLongitude(),
                    ngo.getLatitude(), ngo.getLongitude()
            );

            if (distance <= 5.0) {
                FoodAlertDTO alert = FoodAlertDTO.builder()
                        .listingId(listing.getId())
                        .foodName(listing.getFoodName())
                        .quantity(listing.getQuantity())
                        .location(listing.getLocation())
                        .distanceKm(distance)
                        .expiresAt(listing.getExpiresAt() != null ? listing.getExpiresAt().toString() : null)
                        .donorName(listing.getDonor().getName())
                        .type("NEW_FOOD")
                        .build();

                // Send WebSocket Alert to topic
                messagingTemplate.convertAndSend("/topic/food-alerts", alert);

                // Save Notification to Database
                Notification notification = Notification.builder()
                        .user(ngo)
                        .listing(listing)
                        .type(NotificationType.NEW_FOOD)
                        .message("New food listing available: " + listing.getFoodName() + " at " + listing.getLocation() + " (" + String.format("%.2f", distance) + " km away)")
                        .isRead(false)
                        .build();
                notificationRepository.save(notification);

                // Send Web Push Notification
                pushNotificationService.sendPushNotification(
                        ngo.getId(),
                        "New food nearby!",
                        listing.getFoodName() + " (" + listing.getQuantity() + " meals) available at " + listing.getLocation() + " (" + String.format("%.1f", distance) + " km away).",
                        "/ngo/dashboard"
                );
            }
        }
    }

    public void sendSOSAlert(FoodListing listing) {
        List<User> ngos = userRepository.findByRole(Role.NGO);
        String donorCity = listing.getDonor().getCity();

        for (User ngo : ngos) {
            // Match citywide
            if (donorCity != null && donorCity.equalsIgnoreCase(ngo.getCity())) {
                double distance = 0.0;
                if (listing.getLatitude() != null && listing.getLongitude() != null &&
                    ngo.getLatitude() != null && ngo.getLongitude() != null) {
                    distance = calculateDistance(
                            listing.getLatitude(), listing.getLongitude(),
                            ngo.getLatitude(), ngo.getLongitude()
                    );
                }

                FoodAlertDTO alert = FoodAlertDTO.builder()
                        .listingId(listing.getId())
                        .foodName(listing.getFoodName())
                        .quantity(listing.getQuantity())
                        .location(listing.getLocation())
                        .distanceKm(distance)
                        .expiresAt(listing.getExpiresAt() != null ? listing.getExpiresAt().toString() : null)
                        .donorName(listing.getDonor().getName())
                        .type("SOS")
                        .build();

                // Broadcast via STOMP /topic/food-sos
                messagingTemplate.convertAndSend("/topic/food-sos", alert);

                // Save SOS Notification
                Notification notification = Notification.builder()
                        .user(ngo)
                        .listing(listing)
                        .type(NotificationType.SOS)
                        .message("SOS Alert! Urgent food rescue needed at " + listing.getLocation() + " (" + listing.getFoodName() + ")")
                        .isRead(false)
                        .build();
                notificationRepository.save(notification);

                // Send Web Push Notification for SOS
                pushNotificationService.sendPushNotification(
                        ngo.getId(),
                        "🚨 URGENT SOS ALERT!",
                        "Urgent rescue needed for " + listing.getFoodName() + " (" + listing.getQuantity() + " meals) at " + listing.getLocation() + "!",
                        "/ngo/dashboard"
                );
            }
        }
    }

    public double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        double rlat1 = Math.toRadians(lat1);
        double rlat2 = Math.toRadians(lat2);
        double rlngDiff = Math.toRadians(lng2 - lng1);

        double cosVal = Math.cos(rlat1) * Math.cos(rlat2) * Math.cos(rlngDiff) + Math.sin(rlat1) * Math.sin(rlat2);
        // Clamp cosVal between -1 and 1 to prevent NaN from precision errors
        cosVal = Math.max(-1.0, Math.min(1.0, cosVal));

        return 6371 * Math.acos(cosVal);
    }

    public void broadcastLiveImpact() {
        try {
            com.foodbridge.dto.LiveImpactDTO totals = impactCalculatorService.getPlatformTotals();
            messagingTemplate.convertAndSend("/topic/live-impact", totals);
        } catch (Exception e) {
            System.err.println("Failed to broadcast live impact: " + e.getMessage());
        }
    }
}
