package com.caboolo.backend.ratingandreview.dto;

import com.caboolo.backend.ratingandreview.enums.ReviewTagType;

import java.util.List;

public class ReviewResponseDto {
    private Long reviewedUserId;

    private Integer rating;

    private List<ReviewTagType> tags;

    private String comment;

    private Boolean wouldRideAgain;
}
