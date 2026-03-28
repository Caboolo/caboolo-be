package com.caboolo.backend.review.dto;

import com.caboolo.backend.review.enums.ReviewTagType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserReviewDto {
    private String toUserId;
    private Integer rating;
    private Boolean rideAgain;
    private Set<ReviewTagType> tags;
    private String comment;

    public static interface ToUserIdStep {
        RatingStep withToUserId(String toUserId);
    }

    public static interface RatingStep {
        RideAgainStep withRating(Integer rating);
    }

    public static interface RideAgainStep {
        TagsStep withRideAgain(Boolean rideAgain);
    }

    public static interface TagsStep {
        CommentStep withTags(Set<ReviewTagType> tags);
    }

    public static interface CommentStep {
        BuildStep withComment(String comment);
    }

    public static interface BuildStep {
        UserReviewDto build();
    }

    public static class Builder implements ToUserIdStep, RatingStep, RideAgainStep, TagsStep, CommentStep, BuildStep {
        private String toUserId;
        private Integer rating;
        private Boolean rideAgain;
        private Set<ReviewTagType> tags;
        private String comment;

        public static ToUserIdStep builder() {
            return new Builder();
        }

        @Override
        public RatingStep withToUserId(String toUserId) {
            this.toUserId = toUserId;
            return this;
        }

        @Override
        public RideAgainStep withRating(Integer rating) {
            this.rating = rating;
            return this;
        }

        @Override
        public TagsStep withRideAgain(Boolean rideAgain) {
            this.rideAgain = rideAgain;
            return this;
        }

        @Override
        public CommentStep withTags(Set<ReviewTagType> tags) {
            this.tags = tags;
            return this;
        }

        @Override
        public BuildStep withComment(String comment) {
            this.comment = comment;
            return this;
        }

        @Override
        public UserReviewDto build() {
            return new UserReviewDto(toUserId, rating, rideAgain, tags, comment);
        }
    }
}
