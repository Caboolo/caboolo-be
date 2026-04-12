package com.caboolo.backend.ride.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CrewMemberDto {
    private String userId;
    private String name;
    private String imageUrl;
    private Double avgRating;
    private Integer totalRides;

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
        BuildStep withTotalRides(Integer totalRides);
    }

    public static interface BuildStep {
        CrewMemberDto build();
    }

    public static class Builder implements UserIdStep, NameStep, ImageUrlStep, AvgRatingStep, TotalRidesStep, BuildStep {
        private String userId;
        private String name;
        private String imageUrl;
        private Double avgRating;
        private Integer totalRides;

        private Builder() {
        }

        public static UserIdStep crewMemberDto() {
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
        public BuildStep withTotalRides(Integer totalRides) {
            this.totalRides = totalRides;
            return this;
        }

        @Override
        public CrewMemberDto build() {
            return new CrewMemberDto(
                    this.userId,
                    this.name,
                    this.imageUrl,
                    this.avgRating,
                    this.totalRides
            );
        }
    }
}
