package com.foodbridge.dto;

import java.util.List;

public class RouteRequestDTO {
    private List<Long> listingIds;

    public RouteRequestDTO() {
    }

    public RouteRequestDTO(List<Long> listingIds) {
        this.listingIds = listingIds;
    }

    public List<Long> getListingIds() {
        return listingIds;
    }

    public void setListingIds(List<Long> listingIds) {
        this.listingIds = listingIds;
    }
}
