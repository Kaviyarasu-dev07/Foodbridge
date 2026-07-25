package com.foodbridge.scheduler;

import com.foodbridge.entity.FoodListing;
import com.foodbridge.entity.FoodListing.ListingStatus;
import com.foodbridge.entity.Notification;
import com.foodbridge.entity.Notification.NotificationType;
import com.foodbridge.repository.FoodListingRepository;
import com.foodbridge.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class ExpiredListingScheduler {

    private static final Logger logger = LoggerFactory.getLogger(ExpiredListingScheduler.class);

    @Autowired
    private FoodListingRepository foodListingRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void checkExpiredListings() {
        logger.info("Scanning for expired food listings...");
        LocalDateTime now = LocalDateTime.now();
        List<FoodListing> expiredListings = foodListingRepository.findByStatusAndExpiresAtBefore(
                ListingStatus.ACTIVE, now
        );

        if (!expiredListings.isEmpty()) {
            logger.info("Found {} expired listings to update", expiredListings.size());
            for (FoodListing listing : expiredListings) {
                listing.setStatus(ListingStatus.EXPIRED);
                foodListingRepository.save(listing);

                // Save notification for donor
                Notification notification = Notification.builder()
                        .user(listing.getDonor())
                        .listing(listing)
                        .type(NotificationType.EXPIRED)
                        .message("Your food listing has expired: " + listing.getFoodName())
                        .isRead(false)
                        .build();
                notificationRepository.save(notification);

                logger.info("Listing ID {} ('{}') marked as EXPIRED. Notification sent to donor '{}'",
                        listing.getId(), listing.getFoodName(), listing.getDonor().getEmail());
            }
        }
    }
}
