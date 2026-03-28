package com.caboolo.backend.review.controller;

import com.caboolo.backend.core.controller.BaseController;
import com.caboolo.backend.core.dto.RestEntity;
import com.caboolo.backend.review.dto.*;
import com.caboolo.backend.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ride/review")
public class ReviewController extends BaseController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping("")
    public RestEntity<RideReviewRequestDto> getListOfCoPassengers(@RequestParam Long rideId) {
        return successResponse(reviewService.getListOfCoPassengers(rideId));
    }

    @PostMapping("")
    public RestEntity<Void> submitReview(@RequestBody RideReviewResponseDto request) {
        reviewService.submitReview(request);
        return successResponse("Review submitted successfully");
    }

    @GetMapping("/user")
    public RestEntity<UserProfileDto> getProfileDetails(@RequestParam Long userId) {
        return successResponse(reviewService.getMyProfileHeader(userId));
    }

    @GetMapping("/user/reviews")
    public RestEntity<List<ReviewDto>> getProfileReviews(@RequestParam Long userId) {
        return successResponse(reviewService.getMyProfileDetail(userId));
    }

    @GetMapping("/riderProfile")
    public RestEntity<RiderProfileDto> getRiderProfile(@RequestParam Long userId) {
        return successResponse(reviewService.getCoTravellerProfile(userId));
    }
}
