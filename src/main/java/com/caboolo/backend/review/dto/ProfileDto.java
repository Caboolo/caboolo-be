package com.caboolo.backend.review.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfileDto {
    private String userId;
    private String name;
    private Integer numberOfRides;
    private Double avgRating;
    private Integer noOfReviews;
    private Map<String, Integer> tagCountMap;

    public static interface UserIdStep {
        NameStep withUserId(String userId);
    }

    public static interface NameStep {
        NumberOfRidesStep withName(String name);
    }

    public static interface NumberOfRidesStep {
        AvgRatingStep withNumberOfRides(Integer numberOfRides);
    }

    public static interface AvgRatingStep {
        NoOfReviewsStep withAvgRating(Double avgRating);
    }

    public static interface NoOfReviewsStep {
        TagCountMapStep withNoOfReviews(Integer noOfReviews);
    }

    public static interface TagCountMapStep {
        BuildStep withTagCountMap(Map<String, Integer> tagCountMap);
    }

    public static interface BuildStep {
        ProfileDto build();
    }

    public static class Builder
        implements UserIdStep, NameStep, NumberOfRidesStep, AvgRatingStep, NoOfReviewsStep, TagCountMapStep, BuildStep {
        private String userId;
        private String name;
        private Integer numberOfRides;
        private Double avgRating;
        private Integer noOfReviews;
        private Map<String, Integer> tagCountMap;

        private Builder() {
        }

        public static UserIdStep profileDto() {
            return new Builder();
        }

        @Override
        public NameStep withUserId(String userId) {
            this.userId = userId;
            return this;
        }

        @Override
        public NumberOfRidesStep withName(String name) {
            this.name = name;
            return this;
        }

        @Override
        public AvgRatingStep withNumberOfRides(Integer numberOfRides) {
            this.numberOfRides = numberOfRides;
            return this;
        }

        @Override
        public NoOfReviewsStep withAvgRating(Double avgRating) {
            this.avgRating = avgRating;
            return this;
        }

        @Override
        public TagCountMapStep withNoOfReviews(Integer noOfReviews) {
            this.noOfReviews = noOfReviews;
            return this;
        }

        @Override
        public BuildStep withTagCountMap(Map<String, Integer> tagCountMap) {
            this.tagCountMap = tagCountMap;
            return this;
        }

        @Override
        public ProfileDto build() {
            return new ProfileDto(
                this.userId,
                this.name,
                this.numberOfRides,
                this.avgRating,
                this.noOfReviews,
                this.tagCountMap
            );
        }
    }
}
