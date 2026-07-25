package com.foodbridge.service;

import com.foodbridge.dto.OptimizedRouteDTO;
import com.foodbridge.dto.RouteStopDTO;
import com.foodbridge.entity.FoodListing;
import com.foodbridge.entity.User;
import com.foodbridge.repository.FoodListingRepository;
import com.foodbridge.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RouteOptimizerService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FoodListingRepository foodListingRepository;

    public OptimizedRouteDTO optimizeRoute(Long ngoId, List<Long> listingIds) {
        User ngo = userRepository.findById(ngoId)
                .orElseThrow(() -> new IllegalArgumentException("NGO not found"));

        List<FoodListing> unvisited = foodListingRepository.findAllById(listingIds);
        if (unvisited.isEmpty()) {
            throw new IllegalArgumentException("No valid listings found for optimization");
        }

        double currentLat = ngo.getLatitude() != null ? ngo.getLatitude() : 13.0827;
        double currentLng = ngo.getLongitude() != null ? ngo.getLongitude() : 80.2707;
        double ngoLat = currentLat;
        double ngoLng = currentLng;

        List<RouteStopDTO> stops = new ArrayList<>();
        List<FoodListing> orderedListings = new ArrayList<>();
        double totalDistance = 0.0;
        int totalMinutes = 0;
        int stopOrder = 1;

        while (!unvisited.isEmpty()) {
            FoodListing nearest = null;
            double minDistance = Double.MAX_VALUE;

            for (FoodListing listing : unvisited) {
                double lat = listing.getLatitude() != null ? listing.getLatitude() : 13.0827;
                double lng = listing.getLongitude() != null ? listing.getLongitude() : 80.2707;
                double dist = haversine(currentLat, currentLng, lat, lng);
                if (dist < minDistance) {
                    minDistance = dist;
                    nearest = listing;
                }
            }

            if (nearest == null) break;
            unvisited.remove(nearest);
            orderedListings.add(nearest);

            double lat = nearest.getLatitude() != null ? nearest.getLatitude() : 13.0827;
            double lng = nearest.getLongitude() != null ? nearest.getLongitude() : 80.2707;

            // Calculate travel time from previous stop at 30 km/h average speed in city
            // time in hours = minDistance / 30.0
            // time in minutes = (minDistance / 30.0) * 60.0 = minDistance * 2.0
            int minutes = (int) Math.round(minDistance * 2.0);
            if (minutes < 1) minutes = 1; // at least 1 min for any hop

            RouteStopDTO stop = new RouteStopDTO(
                    stopOrder++,
                    nearest.getId(),
                    nearest.getFoodName(),
                    nearest.getLocation(),
                    lat,
                    lng,
                    Math.round(minDistance * 100.0) / 100.0,
                    minutes + " min"
            );
            stops.add(stop);

            totalDistance += minDistance;
            totalMinutes += minutes;

            currentLat = lat;
            currentLng = lng;
        }

        // Google Maps URL construction:
        // https://www.google.com/maps/dir/?api=1&origin=NGO_LAT,NGO_LNG&waypoints=LAT1,LNG1|LAT2,LNG2&destination=LAST_LAT,LAST_LNG
        StringBuilder urlBuilder = new StringBuilder("https://www.google.com/maps/dir/?api=1");
        urlBuilder.append("&origin=").append(ngoLat).append(",").append(ngoLng);

        if (orderedListings.size() > 1) {
            StringBuilder waypoints = new StringBuilder();
            for (int i = 0; i < orderedListings.size() - 1; i++) {
                FoodListing fl = orderedListings.get(i);
                double flLat = fl.getLatitude() != null ? fl.getLatitude() : 13.0827;
                double flLng = fl.getLongitude() != null ? fl.getLongitude() : 80.2707;
                if (i > 0) waypoints.append("|");
                waypoints.append(flLat).append(",").append(flLng);
            }
            urlBuilder.append("&waypoints=").append(waypoints.toString());
        }

        FoodListing lastListing = orderedListings.get(orderedListings.size() - 1);
        double lastLat = lastListing.getLatitude() != null ? lastListing.getLatitude() : 13.0827;
        double lastLng = lastListing.getLongitude() != null ? lastListing.getLongitude() : 80.2707;
        urlBuilder.append("&destination=").append(lastLat).append(",").append(lastLng);

        return new OptimizedRouteDTO(stops, Math.round(totalDistance * 100.0) / 100.0, totalMinutes, urlBuilder.toString());
    }

    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371.0; // Radius of Earth in kilometers
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
