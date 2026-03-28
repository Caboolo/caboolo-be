package com.caboolo.backend.review.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProfileHeaderDto {
    private String name;
    private Double avgRating;
    private Map<String, Integer> tagCountMap;
    private Integer numberOfRides;
    private Integer totalReviews;

    public static interface NameStep {
        AvgRatingStep withName(String name);
    }

    public static interface AvgRatingStep {
        TagCountMapStep withAvgRating(Double avgRating);
    }

    public static interface TagCountMapStep {
        NumberOfRidesStep withTagCountMap(Map<String, Integer> tagCountMap);
    }

    public static interface NumberOfRidesStep {
        TotalReviewsStep withNumberOfRides(Integer numberOfRides);
    }

    public static interface TotalReviewsStep {
        BuildStep withTotalReviews(Integer totalReviews);
    }

    public static interface BuildStep {
        ProfileHeaderDto build();
    }

    public static class Builder implements NameStep, AvgRatingStep, TagCountMapStep, NumberOfRidesStep, TotalReviewsStep, BuildStep {
        private String name;
        private Double avgRating;
        private Map<String, Integer> tagCountMap;
        private Integer numberOfRides;
        private Integer totalReviews;

        public static NameStep builder() {
            return new Builder();
        }

        @Override
        public AvgRatingStep withName(String name) {
            this.name = name;
            return this;
        }

        @Override
        public TagCountMapStep withAvgRating(Double avgRating) {
            this.avgRating = avgRating;
            return this;
        }

        @Override
        public NumberOfRidesStep withTagCountMap(Map<String, Integer> tagCountMap) {
            this.tagCountMap = tagCountMap;
            return this;
        }

        @Override
        public TotalReviewsStep withNumberOfRides(Integer numberOfRides) {
            this.numberOfRides = numberOfRides;
            return this;
        }

        @Override
        public BuildStep withTotalReviews(Integer totalReviews) {
            this.totalReviews = totalReviews;
            return this;
        }

        @Override
        public ProfileHeaderDto build() {
            return new ProfileHeaderDto(name, avgRating, tagCountMap, numberOfRides, totalReviews);
        }
    }
}
