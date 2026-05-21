package com.caboolo.backend.review.dto;

import com.caboolo.backend.review.enums.ReviewTag;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserReviewDto {
    private String toUserId; // userId of the rider we are rating
    private MinProfileDto toUserProfile;
    private Integer rating;
    private Boolean rideAgain;
    private Set<ReviewTag> tags;
    private String comment;

    public static interface ToUserIdStep {
        ToUserProfileStep withToUserId(String toUserId);
    }

    public static interface ToUserProfileStep {
        RatingStep withToUserProfile(MinProfileDto toUserProfile);
    }

    public static interface RatingStep {
        RideAgainStep withRating(Integer rating);
    }

    public static interface RideAgainStep {
        TagsStep withRideAgain(Boolean rideAgain);
    }

    public static interface TagsStep {
        CommentStep withTags(Set<ReviewTag> tags);
    }

    public static interface CommentStep {
        BuildStep withComment(String comment);
    }

    public static interface BuildStep {
        UserReviewDto build();
    }

    public static class Builder implements ToUserIdStep, ToUserProfileStep, RatingStep, RideAgainStep, TagsStep, CommentStep, BuildStep {
        private String toUserId;
        private MinProfileDto toUserProfile;
        private Integer rating;
        private Boolean rideAgain;
        private Set<ReviewTag> tags;
        private String comment;

        public static ToUserIdStep builder() {
            return new Builder();
        }

        @Override
        public ToUserProfileStep withToUserId(String toUserId) {
            this.toUserId = toUserId;
            return this;
        }

        @Override
        public RatingStep withToUserProfile(MinProfileDto toUserProfile) {
            this.toUserProfile = toUserProfile;
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
        public CommentStep withTags(Set<ReviewTag> tags) {
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
            return new UserReviewDto(toUserId, toUserProfile, rating, rideAgain, tags, comment);
        }
    }
}
