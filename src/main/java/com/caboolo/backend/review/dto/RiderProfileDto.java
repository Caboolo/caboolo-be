package com.caboolo.backend.review.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
public class RiderProfileDto extends ProfileDto {
    private Double trustScore;
    private Map<Integer, Integer> ratingBreakdown;

    public RiderProfileDto(String userId, String name, Integer numberOfRides, Double avgRating, Integer noOfReviews,
                           Map<String, Integer> tagCountMap, Double trustScore, Map<Integer, Integer> ratingBreakdown) {
        super(userId, name, numberOfRides, avgRating, noOfReviews, tagCountMap);
        this.trustScore = trustScore;
        this.ratingBreakdown = ratingBreakdown;
    }

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
        TrustScoreStep withTagCountMap(Map<String, Integer> tagCountMap);
    }

    public static interface TrustScoreStep {
        RatingBreakdownStep withTrustScore(Double trustScore);
    }

    public static interface RatingBreakdownStep {
        BuildStep withRatingBreakdown(Map<Integer, Integer> ratingBreakdown);
    }

    public static interface BuildStep {
        RiderProfileDto build();
    }

    public static class Builder
        implements UserIdStep, NameStep, NumberOfRidesStep, AvgRatingStep, NoOfReviewsStep, TagCountMapStep,
        TrustScoreStep, RatingBreakdownStep, BuildStep {
        private String userId;
        private String name;
        private Integer numberOfRides;
        private Double avgRating;
        private Integer noOfReviews;
        private Map<String, Integer> tagCountMap;
        private Double trustScore;
        private Map<Integer, Integer> ratingBreakdown;

        private Builder() {
        }

        public static UserIdStep riderProfileDto() {
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
        public TrustScoreStep withTagCountMap(Map<String, Integer> tagCountMap) {
            this.tagCountMap = tagCountMap;
            return this;
        }

        @Override
        public RatingBreakdownStep withTrustScore(Double trustScore) {
            this.trustScore = trustScore;
            return this;
        }

        @Override
        public BuildStep withRatingBreakdown(Map<Integer, Integer> ratingBreakdown) {
            this.ratingBreakdown = ratingBreakdown;
            return this;
        }

        @Override
        public RiderProfileDto build() {
            return new RiderProfileDto(
                this.userId,
                this.name,
                this.numberOfRides,
                this.avgRating,
                this.noOfReviews,
                this.tagCountMap,
                this.trustScore,
                this.ratingBreakdown
            );
        }
    }
}
