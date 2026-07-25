package com.foodbridge.service;

import com.foodbridge.entity.Claim;
import com.foodbridge.entity.Claim.ClaimStatus;
import com.foodbridge.entity.FoodListing;
import com.foodbridge.entity.FoodListing.ListingStatus;
import com.foodbridge.entity.FoodSOS;
import com.foodbridge.entity.FoodSOS.SOSStatus;
import com.foodbridge.entity.User;
import com.foodbridge.repository.ClaimRepository;
import com.foodbridge.repository.FoodListingRepository;
import com.foodbridge.repository.FoodSOSRepository;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
@Transactional
public class FoodListingService {

    @Autowired
    private FoodListingRepository foodListingRepository;

    @Autowired
    private ClaimRepository claimRepository;

    @Autowired
    private FoodSOSRepository foodSOSRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private BadgeService badgeService;

    @Autowired
    private ImpactService impactService;

    @Autowired
    private PushNotificationService pushNotificationService;

    public FoodListing createListing(FoodListing listing, User donor) {
        listing.setDonor(donor);
        listing.setStatus(ListingStatus.ACTIVE);

        if (listing.getExpiresAt() == null && listing.getPickupMinutes() != null) {
            listing.setExpiresAt(LocalDateTime.now().plusMinutes(listing.getPickupMinutes()));
        } else if (listing.getExpiresAt() == null) {
            listing.setExpiresAt(LocalDateTime.now().plusHours(4)); // default 4 hours
        }

        // Save first to get the ID
        FoodListing savedListing = foodListingRepository.save(listing);

        // Generate QR Code containing listing info
        String qrContent = "FoodBridge Listing ID: " + savedListing.getId() + "\nFood: " + savedListing.getFoodName() + "\nQuantity: " + savedListing.getQuantity();
        String qrCodeBase64 = generateQRCodeBase64(qrContent);
        savedListing.setQrCode(qrCodeBase64);

        // Save again with QR code
        savedListing = foodListingRepository.save(savedListing);

        // Trigger real-time notifications
        notificationService.sendFoodAlert(savedListing);

        return savedListing;
    }

    public List<FoodListing> getNearbyListings(double lat, double lng) {
        List<FoodListing> activeListings = foodListingRepository.findByStatus(ListingStatus.ACTIVE);
        List<FoodListing> nearbyListings = new ArrayList<>();

        for (FoodListing listing : activeListings) {
            if (listing.getLatitude() == null || listing.getLongitude() == null) {
                continue;
            }

            double distance = notificationService.calculateDistance(
                    lat, lng,
                    listing.getLatitude(), listing.getLongitude()
            );

            if (distance <= 5.0) {
                nearbyListings.add(listing);
            }
        }
        return nearbyListings;
    }

    public Claim claimListing(Long id, User ngo) {
        FoodListing listing = foodListingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Listing not found with ID: " + id));

        if (listing.getStatus() != ListingStatus.ACTIVE) {
            throw new IllegalStateException("Listing is not ACTIVE; current status is: " + listing.getStatus());
        }

        listing.setStatus(ListingStatus.CLAIMED);
        foodListingRepository.save(listing);

        Claim claim = Claim.builder()
                .listing(listing)
                .ngo(ngo)
                .status(ClaimStatus.CLAIMED)
                .claimedAt(LocalDateTime.now())
                .build();

        Claim savedClaim = claimRepository.save(claim);

        // Update impact stats and award badges
        try {
            int quantity = parseQuantity(listing.getQuantity());
            
            // Update stats for NGO and Donor
            impactService.updateImpactStats(ngo.getId(), quantity);
            impactService.updateImpactStats(listing.getDonor().getId(), quantity);

            // Check badges for NGO and Donor
            badgeService.checkAndAwardBadges(ngo.getId());
            badgeService.checkAndAwardBadges(listing.getDonor().getId());

            // Broadcast live platform impact statistics update
            notificationService.broadcastLiveImpact();

            // Send push notification to the donor
            pushNotificationService.sendPushNotification(
                    listing.getDonor().getId(),
                    "Food listing claimed!",
                    "Your listing '" + listing.getFoodName() + "' has been successfully claimed by " + ngo.getName() + ".",
                    "/donor/dashboard"
            );
        } catch (Exception e) {
            // Log error and continue to ensure claim transaction succeeds
            System.err.println("Error processing gamification badges/impact stats/push: " + e.getMessage());
        }

        return savedClaim;
    }

    public FoodSOS triggerSOS(Long id) {
        FoodListing listing = foodListingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Listing not found with ID: " + id));

        if (listing.getStatus() != ListingStatus.ACTIVE) {
            throw new IllegalStateException("Listing is not ACTIVE; cannot trigger SOS.");
        }

        FoodSOS sos = FoodSOS.builder()
                .listing(listing)
                .status(SOSStatus.ACTIVE)
                .triggeredAt(LocalDateTime.now())
                .build();

        foodSOSRepository.save(sos);

        // Send citywide notification alert
        notificationService.sendSOSAlert(listing);

        return sos;
    }

    public List<FoodListing> getMyListings(User donor) {
        return foodListingRepository.findByDonor(donor);
    }

    public List<FoodListing> getAllListings() {
        return foodListingRepository.findAll();
    }

    private String generateQRCodeBase64(String text) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, 250, 250);
            ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
            byte[] pngData = pngOutputStream.toByteArray();
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(pngData);
        } catch (Exception e) {
            // Fallback mock QR
            return "MOCK_QR_CODE_FOR_" + Base64.getEncoder().encodeToString(text.getBytes());
        }
    }

    private int parseQuantity(String quantityStr) {
        if (quantityStr == null) return 0;
        try {
            String clean = quantityStr.replaceAll("[^0-9]", "");
            if (!clean.isEmpty()) {
                return Integer.parseInt(clean);
            }
        } catch (Exception e) {
            // fallback
        }
        return 10; // default meals value if parsing fails
    }
}
