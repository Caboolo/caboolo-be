package com.caboolo.backend.review.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RideReviewResponseDto {
    @NotNull(message = "rideId cannot be null")
    private String rideId;

    @NotBlank(message = "byUserId cannot be null or blank")
    private String byUserId;

    @NotNull(message = "reviews cannot be null")
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

        private Builder() {
        }

        public static RideIdStep rideReviewResponseDto() {
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
            return new RideReviewResponseDto(
                this.rideId,
                this.byUserId,
                this.reviews
            );
        }
    }
}
