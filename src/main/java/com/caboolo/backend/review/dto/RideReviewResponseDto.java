package com.caboolo.backend.review.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RideReviewResponseDto {
    private String rideId;
    private String byUserId;
    private List<UserReviewDto> reviews;

    public static interface RideIdStep {
        ByUserIdStep withRideId(String rideId);
    }

    public static interface ByUserIdStep {
        ReviewsStep withByUserId(String byUserId);
    }

    public static interface ReviewsStep {
        BuildStep withReviews(List<UserReviewDto> reviews);
    }

    public static interface BuildStep {
        RideReviewResponseDto build();
    }

    public static class Builder implements RideIdStep, ByUserIdStep, ReviewsStep, BuildStep {
        private String rideId;
        private String byUserId;
        private List<UserReviewDto> reviews;

        public static RideIdStep builder() {
            return new Builder();
        }

        @Override
        public ByUserIdStep withRideId(String rideId) {
            this.rideId = rideId;
            return this;
        }

        @Override
        public ReviewsStep withByUserId(String byUserId) {
            this.byUserId = byUserId;
            return this;
        }

        @Override
        public BuildStep withReviews(List<UserReviewDto> reviews) {
            this.reviews = reviews;
            return this;
        }

        @Override
        public RideReviewResponseDto build() {
            return new RideReviewResponseDto(rideId, byUserId, reviews);
        }
    }
}
