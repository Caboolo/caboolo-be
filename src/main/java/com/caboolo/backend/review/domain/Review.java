package com.caboolo.backend.review.domain;

import com.caboolo.backend.core.domain.GenericIdEntity;
import com.caboolo.backend.review.converter.ReviewTagSetConverter;
import com.caboolo.backend.review.enums.ReviewTagType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Set;

@Entity
@Table(name = "reviews")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Review extends GenericIdEntity {

    @Column(name = "review_id")
    private Long reviewId;

    @Column(name = "ride_id", nullable = false)
    private Long rideId;

    @Column(name = "for_user_id", nullable = false)
    private Long forUserId;

    @Column(name = "by_user_id", nullable = false)
    private Long byUserId;

    @Column(name = "rating", nullable = false)
    private Integer rating;

    @Column(name = "comment")
    private String comment;

    @Column(name = "ride_again")
    private Boolean rideAgain;

    @Column(name = "tags")
    @Convert(converter = ReviewTagSetConverter.class)
    private Set<ReviewTagType> tags;

    public static interface ReviewIdStep {
        RideIdStep withReviewId(Long reviewId);
    }

    public static interface RideIdStep {
        ForUserIdStep withRideId(Long rideId);
    }

    public static interface ForUserIdStep {
        ByUserIdStep withForUserId(Long forUserId);
    }

    public static interface ByUserIdStep {
        RatingStep withByUserId(Long byUserId);
    }

    public static interface RatingStep {
        CommentStep withRating(Integer rating);
    }

    public static interface CommentStep {
        RideAgainStep withComment(String comment);
    }

    public static interface RideAgainStep {
        TagsStep withRideAgain(Boolean rideAgain);
    }

    public static interface TagsStep {
        BuildStep withTags(Set<ReviewTagType> tags);
    }

    public static interface BuildStep {
        Review build();
    }


    public static class Builder implements ReviewIdStep, RideIdStep, ForUserIdStep, ByUserIdStep, RatingStep, CommentStep, RideAgainStep, TagsStep, BuildStep {
        private Long reviewId;
        private Long rideId;
        private Long forUserId;
        private Long byUserId;
        private Integer rating;
        private String comment;
        private Boolean rideAgain;
        private Set<ReviewTagType> tags;

        private Builder() {
        }

        public static ReviewIdStep review() {
            return new Builder();
        }

        @Override
        public RideIdStep withReviewId(Long reviewId) {
            this.reviewId = reviewId;
            return this;
        }

        @Override
        public ForUserIdStep withRideId(Long rideId) {
            this.rideId = rideId;
            return this;
        }

        @Override
        public ByUserIdStep withForUserId(Long forUserId) {
            this.forUserId = forUserId;
            return this;
        }

        @Override
        public RatingStep withByUserId(Long byUserId) {
            this.byUserId = byUserId;
            return this;
        }

        @Override
        public CommentStep withRating(Integer rating) {
            this.rating = rating;
            return this;
        }

        @Override
        public RideAgainStep withComment(String comment) {
            this.comment = comment;
            return this;
        }

        @Override
        public TagsStep withRideAgain(Boolean rideAgain) {
            this.rideAgain = rideAgain;
            return this;
        }

        @Override
        public BuildStep withTags(Set<ReviewTagType> tags) {
            this.tags = tags;
            return this;
        }

        @Override
        public Review build() {
            return new Review(
                    this.reviewId,
                    this.rideId,
                    this.forUserId,
                    this.byUserId,
                    this.rating,
                    this.comment,
                    this.rideAgain,
                    this.tags
            );
        }
    }
}
