package com.caboolo.backend.ratingandreview.service;

import com.caboolo.backend.ratingandreview.dto.MyProfileReviewSummaryDto;
import com.caboolo.backend.ratingandreview.dto.PublicProfileReviewSummaryDto;
import com.caboolo.backend.ratingandreview.dto.SubmitReviewRequestDto;

public interface ReviewService {

    void submitReviews(Long reviewerId, SubmitReviewRequestDto request);

    MyProfileReviewSummaryDto getMyReviews(Long userId);

    PublicProfileReviewSummaryDto getUserReviews(Long userId);
}
