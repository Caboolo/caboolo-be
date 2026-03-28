package com.caboolo.backend.review.service;

import com.caboolo.backend.review.dto.*;
import java.util.List;

public interface ReviewService {

    void submitReview(RideReviewResponseDto request);

    RideReviewRequestDto getListOfCoPassengers(Long rideId);

    List<ReviewDto> getReviewDtoList(String userId);

    RiderProfileDto getCoTravellerProfile(String userId);
}
