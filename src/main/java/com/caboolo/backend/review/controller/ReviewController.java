package com.caboolo.backend.review.controller;

import com.caboolo.backend.core.controller.BaseController;
import com.caboolo.backend.core.dto.RestEntity;
import com.caboolo.backend.review.dto.*;
import com.caboolo.backend.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController extends BaseController {

    private final ReviewService reviewService;

    @GetMapping("/co-passengers")
    public RestEntity<RideReviewRequestDto> getListOfCoPassengers(@RequestParam Long rideId) {
        return successResponse(reviewService.getListOfCoPassengers(rideId));
    }

    @PostMapping("/submit")
    public RestEntity<Void> submitReview(@RequestBody RideReviewResponseDto request) {
        reviewService.submitReview(request);
        return successResponse("Review submitted successfully");
    }

    @GetMapping("/profile/header")
    public RestEntity<UserProfileDto> getMyProfileHeader(@RequestParam String userId) {
        return successResponse(reviewService.getMyProfileHeader(userId));
    }

    @GetMapping("/profile/details")
    public RestEntity<List<ReviewDto>> getMyProfileDetail(@RequestParam String userId) {
        return successResponse(reviewService.getMyProfileDetail(userId));
    }

    @GetMapping("/profile/co-traveller")
    public RestEntity<RiderProfileDto> getCoTravellerProfile(@RequestParam String userId) {
        return successResponse(reviewService.getCoTravellerProfile(userId));
    }
}
