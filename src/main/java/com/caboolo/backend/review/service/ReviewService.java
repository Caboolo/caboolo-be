package com.caboolo.backend.review.service;

import com.caboolo.backend.review.dto.MyProfileReviewSummaryDto;
import com.caboolo.backend.review.dto.PublicProfileReviewSummaryDto;
import com.caboolo.backend.review.dto.SubmitReviewRequestDto;

public interface ReviewService {

    void submitReviews(Long reviewerId, SubmitReviewRequestDto request);

    MyProfileReviewSummaryDto getMyReviews(Long userId);

    PublicProfileReviewSummaryDto getUserReviews(Long userId);
}
