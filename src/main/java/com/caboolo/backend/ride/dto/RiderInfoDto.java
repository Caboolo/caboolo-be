package com.caboolo.backend.ride.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RiderInfoDto {
    private String userId;
    private String name;
    private String imageUrl;
    private Double avgRating;
    private boolean isFlightVerified;

    public static interface UserIdStep {
        NameStep withUserId(String userId);
    }

    public static interface NameStep {
        ImageUrlStep withName(String name);
    }

    public static interface ImageUrlStep {
        AvgRatingStep withImageUrl(String imageUrl);
    }

    public static interface AvgRatingStep {
        IsFlightVerifiedStep withAvgRating(Double avgRating);
    }

    public static interface IsFlightVerifiedStep {
        BuildStep withIsFlightVerified(boolean isFlightVerified);
    }

    public static interface BuildStep {
        RiderInfoDto build();
    }


    public static class Builder implements UserIdStep, NameStep, ImageUrlStep, AvgRatingStep, IsFlightVerifiedStep, BuildStep {
        private String userId;
        private String name;
        private String imageUrl;
        private Double avgRating;
        private boolean isFlightVerified;

        private Builder() {
        }

        public static UserIdStep passengerInfoDto() {
            return new Builder();
        }

        @Override
        public NameStep withUserId(String userId) {
            this.userId = userId;
            return this;
        }

        @Override
        public ImageUrlStep withName(String name) {
            this.name = name;
            return this;
        }

        @Override
        public AvgRatingStep withImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
            return this;
        }

        @Override
        public IsFlightVerifiedStep withAvgRating(Double avgRating) {
            this.avgRating = avgRating;
            return this;
        }

        @Override
        public BuildStep withIsFlightVerified(boolean isFlightVerified) {
            this.isFlightVerified = isFlightVerified;
            return this;
        }

        @Override
        public RiderInfoDto build() {
            return new RiderInfoDto(
                    this.userId,
                    this.name,
                    this.imageUrl,
                    this.avgRating,
                    this.isFlightVerified
            );
        }
    }
}
