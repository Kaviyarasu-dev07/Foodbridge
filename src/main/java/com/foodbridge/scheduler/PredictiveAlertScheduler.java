package com.foodbridge.scheduler;

import com.foodbridge.dto.DonorPatternDTO;
import com.foodbridge.dto.PredictiveAlertPayload;
import com.foodbridge.entity.Notification;
import com.foodbridge.entity.User;
import com.foodbridge.repository.NotificationRepository;
import com.foodbridge.repository.UserRepository;
import com.foodbridge.service.DonorPatternAnalyser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class PredictiveAlertScheduler {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DonorPatternAnalyser donorPatternAnalyser;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Scheduled(cron = "0 0 * * * *")
    public void runPredictiveAlerts() {
        checkAndSendPredictiveAlerts(LocalDateTime.now());
    }

    public void checkAndSendPredictiveAlerts(LocalDateTime currentTime) {
        int currentDayOfWeek = currentTime.getDayOfWeek().getValue();
        int currentHour = currentTime.getHour();
        int targetHour = (currentHour + 1) % 24;
        int targetDayOfWeek = currentDayOfWeek;
        if (currentHour + 1 >= 24) {
            targetDayOfWeek = (currentDayOfWeek % 7) + 1;
        }

        List<User> donors = userRepository.findByRole(User.Role.DONOR);
        List<User> ngos = userRepository.findByRole(User.Role.NGO);

        for (User donor : donors) {
            List<DonorPatternDTO> patterns = donorPatternAnalyser.analyseDonorPatterns(donor.getId());
            for (DonorPatternDTO pattern : patterns) {
                if (pattern.getDayOfWeek() == targetDayOfWeek && pattern.getHourOfDay() == targetHour) {
                    for (User ngo : ngos) {
                        double ngoLat = ngo.getLatitude() != null ? ngo.getLatitude() : 13.0827;
                        double ngoLng = ngo.getLongitude() != null ? ngo.getLongitude() : 80.2707;
                        double donorLat = pattern.getLatitude() != null ? pattern.getLatitude() : 13.0827;
                        double donorLng = pattern.getLongitude() != null ? pattern.getLongitude() : 80.2707;

                        double distance = haversine(ngoLat, ngoLng, donorLat, donorLng);
                        if (distance <= 5.0) {
                            String foodMsg = pattern.getCommonFoodType().toLowerCase().replace("_", " ");
                            String message = pattern.getDonorName() + " usually posts " + foodMsg + " around this time. Get ready!";
                            
                            Notification notification = Notification.builder()
                                    .user(ngo)
                                    .type(Notification.NotificationType.PREDICTIVE)
                                    .message(message)
                                    .build();
                            notificationRepository.save(notification);

                            PredictiveAlertPayload payload = new PredictiveAlertPayload(
                                    pattern,
                                    message,
                                    Math.round(distance * 10.0) / 10.0
                            );
                            messagingTemplate.convertAndSend("/topic/predictive-alerts", payload);
                        }
                    }
                }
            }
        }
    }

    public List<PredictiveAlertPayload> getTodayPredictiveAlertsForNgo(User ngo) {
        List<PredictiveAlertPayload> alerts = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        int currentDayOfWeek = now.getDayOfWeek().getValue();

        List<User> donors = userRepository.findByRole(User.Role.DONOR);
        for (User donor : donors) {
            List<DonorPatternDTO> patterns = donorPatternAnalyser.analyseDonorPatterns(donor.getId());
            for (DonorPatternDTO pattern : patterns) {
                if (pattern.getDayOfWeek() == currentDayOfWeek) {
                    double ngoLat = ngo.getLatitude() != null ? ngo.getLatitude() : 13.0827;
                    double ngoLng = ngo.getLongitude() != null ? ngo.getLongitude() : 80.2707;
                    double donorLat = pattern.getLatitude() != null ? pattern.getLatitude() : 13.0827;
                    double donorLng = pattern.getLongitude() != null ? pattern.getLongitude() : 80.2707;

                    double distance = haversine(ngoLat, ngoLng, donorLat, donorLng);
                    if (distance <= 5.0) {
                        String foodMsg = pattern.getCommonFoodType().toLowerCase().replace("_", " ");
                        String message = pattern.getDonorName() + " usually posts " + foodMsg + " around this time. Get ready!";
                        alerts.add(new PredictiveAlertPayload(pattern, message, Math.round(distance * 10.0) / 10.0));
                    }
                }
            }
        }
        return alerts;
    }

    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
