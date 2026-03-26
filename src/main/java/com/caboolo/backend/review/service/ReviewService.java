package com.caboolo.backend.review.service;

import com.caboolo.backend.review.dto.*;
import java.util.List;

public interface ReviewService {

    void submitReview(SubmitReviewRequestDto request);

    CoPassengerResponseDto getListOfCoPassengers(Long rideId);

    ProfileHeaderDto getMyProfileHeader(Long userId);

    List<ReviewMinDto> getMyProfileDetail(Long userId);

    CoTravellerProfileDto getCoTravellerProfile(Long userId);
}
