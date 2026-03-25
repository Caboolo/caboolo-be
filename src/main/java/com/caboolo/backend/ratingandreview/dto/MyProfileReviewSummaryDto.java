package com.caboolo.backend.ratingandreview.dto;

import com.caboolo.backend.ratingandreview.enums.ReviewTagType;

import java.util.List;
import java.util.Map;

public class MyProfileReviewSummaryDto {

    private Double avgRating;
    private Integer totalReviews;

    private Map<ReviewTagType, Integer> tagCounts;

    private List<ReviewResponseDto> reviews;
}
