package com.caboolo.backend.review.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MinProfileDto {
    private Long userId;
    private String name;
    private Double avgRating;

    public static interface UserIdStep {
        NameStep withUserId(Long userId);
    }

    public static interface NameStep {
        AvgRatingStep withName(String name);
    }

    public static interface AvgRatingStep {
        BuildStep withAvgRating(Double avgRating);
    }

    public static interface BuildStep {
        MinProfileDto build();
    }

    public static class Builder implements UserIdStep, NameStep, AvgRatingStep, BuildStep {
        private Long userId;
        private String name;
        private Double avgRating;

        public static UserIdStep builder() {
            return new Builder();
        }

        @Override
        public NameStep withUserId(Long userId) {
            this.userId = userId;
            return this;
        }

        @Override
        public AvgRatingStep withName(String name) {
            this.name = name;
            return this;
        }

        @Override
        public BuildStep withAvgRating(Double avgRating) {
            this.avgRating = avgRating;
            return this;
        }

        @Override
        public MinProfileDto build() {
            return new MinProfileDto(userId, name, avgRating);
        }
    }
}
