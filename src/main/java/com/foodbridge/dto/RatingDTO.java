package com.foodbridge.dto;

public class RatingDTO {
    private Long listingId;
    private Integer score;
    private String comment;

    public RatingDTO() {
    }

    public RatingDTO(Long listingId, Integer score, String comment) {
        this.listingId = listingId;
        this.score = score;
        this.comment = comment;
    }

    public Long getListingId() {
        return listingId;
    }

    public void setListingId(Long listingId) {
        this.listingId = listingId;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
