package com.caboolo.backend.review.service;

import com.caboolo.backend.review.dto.*;
import java.util.List;

public interface ReviewService {

    void submitReview(SubmitReviewRequestDto request);

    CoPassengerResponseDto getListOfCoPassengers(Long rideId);

    ProfileHeaderDto getMyProfileHeader(String userId);

    List<ReviewMinDto> getMyProfileDetail(String userId);

    CoTravellerProfileDto getCoTravellerProfile(String userId);
}
