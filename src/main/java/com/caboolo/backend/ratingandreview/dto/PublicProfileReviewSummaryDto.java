package com.caboolo.backend.ratingandreview.dto;

import com.caboolo.backend.ratingandreview.enums.ReviewTagType;

import java.util.List;
import java.util.Map;

public class PublicProfileReviewSummaryDto {
    private Double avgRating;

    private Map<ReviewTagType, Integer> tagCounts;

    private Double trustScore;

    public static class UserReviewInputDto {
        private Long reviewedUserId;

        private Integer rating;

        private List<ReviewTagType> tags;

        private String comment;

        private Boolean wouldRideAgain;
    }
}
