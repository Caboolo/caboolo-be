package com.caboolo.backend.review.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubmitReviewRequestDto {
    private Long rideId;
    private Long byUserId;
    private List<ReviewItemDto> reviews;

    public static interface RideIdStep {
        ByUserIdStep withRideId(Long rideId);
    }

    public static interface ByUserIdStep {
        ReviewsStep withByUserId(Long byUserId);
    }

    public static interface ReviewsStep {
        BuildStep withReviews(List<ReviewItemDto> reviews);
    }

    public static interface BuildStep {
        SubmitReviewRequestDto build();
    }

    public static class Builder implements RideIdStep, ByUserIdStep, ReviewsStep, BuildStep {
        private Long rideId;
        private Long byUserId;
        private List<ReviewItemDto> reviews;

        public static RideIdStep builder() {
            return new Builder();
        }

        @Override
        public ByUserIdStep withRideId(Long rideId) {
            this.rideId = rideId;
            return this;
        }

        @Override
        public ReviewsStep withByUserId(Long byUserId) {
            this.byUserId = byUserId;
            return this;
        }

        @Override
        public BuildStep withReviews(List<ReviewItemDto> reviews) {
            this.reviews = reviews;
            return this;
        }

        @Override
        public SubmitReviewRequestDto build() {
            return new SubmitReviewRequestDto(rideId, byUserId, reviews);
        }
    }
}
