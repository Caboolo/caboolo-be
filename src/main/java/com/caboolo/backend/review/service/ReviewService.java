package com.caboolo.backend.review.service;

import com.caboolo.backend.review.dto.*;
import java.util.List;

public interface ReviewService {

    void submitReview(RideReviewResponseDto request);

    RideReviewRequestDto getListOfCoPassengers(Long rideId);

    UserProfileDto getMyProfileHeader(String userId);

    List<ReviewDto> getMyProfileDetail(String userId);

    RiderProfileDto getCoTravellerProfile(String userId);
}
