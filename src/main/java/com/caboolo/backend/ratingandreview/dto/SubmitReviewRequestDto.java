package com.caboolo.backend.ratingandreview.dto;

import java.util.List;

public class SubmitReviewRequestDto {
    private Long rideId;

    private List<PublicProfileReviewSummaryDto.UserReviewInputDto> reviews;
}
