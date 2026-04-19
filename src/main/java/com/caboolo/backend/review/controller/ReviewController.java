package com.caboolo.backend.review.controller;

import com.caboolo.backend.core.controller.BaseController;
import com.caboolo.backend.core.dto.RestEntity;
import com.caboolo.backend.review.dto.*;
import com.caboolo.backend.review.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reviews")
public class ReviewController extends BaseController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping("/co-passengers/listing")
    public RestEntity<RideReviewRequestDto> getListOfCoPassengers(@RequestParam String rideId) {
        return successResponse(reviewService.getListOfCoPassengers(rideId));
    }

    @PostMapping("/submit")
    public RestEntity<Void> submitReview(@Valid @RequestBody RideReviewResponseDto request) {
        reviewService.submitReview(request);
        return successResponse("Review submitted successfully");
    }

    @GetMapping("/co-passenger/profile")
    public RestEntity<RiderProfileDto> getCoTravellerProfile(@RequestParam String userId) {
        return successResponse(reviewService.getCoTravellerProfile(userId));
    }
}
