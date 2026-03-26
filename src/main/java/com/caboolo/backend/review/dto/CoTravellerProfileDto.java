package com.caboolo.backend.review.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CoTravellerProfileDto {
    private String name;
    private Integer numberOfRides;
    private Double avgRating;
    private Integer noOfReviews;
    private Double trustScore;
    private Map<String, Integer> tagCountMap;
    private Map<Integer, Integer> ratingBreakdown;

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
        TrustScoreStep withNoOfReviews(Integer noOfReviews);
    }

    public static interface TrustScoreStep {
        TagCountMapStep withTrustScore(Double trustScore);
    }

    public static interface TagCountMapStep {
        RatingBreakdownStep withTagCountMap(Map<String, Integer> tagCountMap);
    }

    public static interface RatingBreakdownStep {
        BuildStep withRatingBreakdown(Map<Integer, Integer> ratingBreakdown);
    }

    public static interface BuildStep {
        CoTravellerProfileDto build();
    }

    public static class Builder implements NameStep, NumberOfRidesStep, AvgRatingStep, NoOfReviewsStep, TrustScoreStep, TagCountMapStep, RatingBreakdownStep, BuildStep {
        private String name;
        private Integer numberOfRides;
        private Double avgRating;
        private Integer noOfReviews;
        private Double trustScore;
        private Map<String, Integer> tagCountMap;
        private Map<Integer, Integer> ratingBreakdown;

        public static NameStep builder() {
            return new Builder();
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
        public TrustScoreStep withNoOfReviews(Integer noOfReviews) {
            this.noOfReviews = noOfReviews;
            return this;
        }

        @Override
        public TagCountMapStep withTrustScore(Double trustScore) {
            this.trustScore = trustScore;
            return this;
        }

        @Override
        public RatingBreakdownStep withTagCountMap(Map<String, Integer> tagCountMap) {
            this.tagCountMap = tagCountMap;
            return this;
        }

        @Override
        public BuildStep withRatingBreakdown(Map<Integer, Integer> ratingBreakdown) {
            this.ratingBreakdown = ratingBreakdown;
            return this;
        }

        @Override
        public CoTravellerProfileDto build() {
            return new CoTravellerProfileDto(name, numberOfRides, avgRating, noOfReviews, trustScore, tagCountMap, ratingBreakdown);
        }
    }
}
