package com.caboolo.backend.ride.dto;

import com.caboolo.backend.ride.enums.RideUserMappingStatus;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PendingRequestDto {
    private String userId;
    private String name;
    private String imageUrl;
    private Double avgRating;
    private Integer totalRides;
    private RideUserMappingStatus status;

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
        TotalRidesStep withAvgRating(Double avgRating);
    }

    public static interface TotalRidesStep {
        StatusStep withTotalRides(Integer totalRides);
    }

    public static interface StatusStep {
        BuildStep withStatus(RideUserMappingStatus status);
    }

    public static interface BuildStep {
        PendingRequestDto build();
    }

    public static class Builder implements UserIdStep, NameStep, ImageUrlStep, AvgRatingStep, TotalRidesStep, StatusStep, BuildStep {
        private String userId;
        private String name;
        private String imageUrl;
        private Double avgRating;
        private Integer totalRides;
        private RideUserMappingStatus status;

        private Builder() {
        }

        public static UserIdStep pendingRequestDto() {
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
        public TotalRidesStep withAvgRating(Double avgRating) {
            this.avgRating = avgRating;
            return this;
        }

        @Override
        public StatusStep withTotalRides(Integer totalRides) {
            this.totalRides = totalRides;
            return this;
        }

        @Override
        public BuildStep withStatus(RideUserMappingStatus status) {
            this.status = status;
            return this;
        }

        @Override
        public PendingRequestDto build() {
            return new PendingRequestDto(
                    this.userId,
                    this.name,
                    this.imageUrl,
                    this.avgRating,
                    this.totalRides,
                    this.status
            );
        }
    }
}
