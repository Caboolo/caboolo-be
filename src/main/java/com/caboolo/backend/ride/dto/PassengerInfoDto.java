package com.caboolo.backend.ride.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PassengerInfoDto {
    private String userId;
    private String name;
    private String imageUrl;
    private Double avgRating;

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
        BuildStep withAvgRating(Double avgRating);
    }

    public static interface BuildStep {
        PassengerInfoDto build();
    }


    public static class Builder implements UserIdStep, NameStep, ImageUrlStep, AvgRatingStep, BuildStep {
        private String userId;
        private String name;
        private String imageUrl;
        private Double avgRating;

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
        public BuildStep withAvgRating(Double avgRating) {
            this.avgRating = avgRating;
            return this;
        }

        @Override
        public PassengerInfoDto build() {
            return new PassengerInfoDto(
                    this.userId,
                    this.name,
                    this.imageUrl,
                    this.avgRating
            );
        }
    }
}
