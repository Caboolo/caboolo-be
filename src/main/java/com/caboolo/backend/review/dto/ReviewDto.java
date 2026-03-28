package com.caboolo.backend.review.dto;

import com.caboolo.backend.review.enums.ReviewTag;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReviewDto {
    private Long byUserId;
    private String rideId;
    private String source;
    private String destination;
    private LocalDateTime date; // rideDate
    private Set<ReviewTag> tags;
    private String comments;
    private Integer rating;

    public static interface ByUserIdStep {
        RideIdStep withByUserId(Long byUserId);
    }

    public static interface RideIdStep {
        SourceStep withRideId(String rideId);
    }

    public static interface SourceStep {
        DestinationStep withSource(String source);
    }

    public static interface DestinationStep {
        DateStep withDestination(String destination);
    }

    public static interface DateStep {
        TagsStep withDate(LocalDateTime date);
    }

    public static interface TagsStep {
        CommentsStep withTags(Set<ReviewTag> tags);
    }

    public static interface CommentsStep {
        RatingStep withComments(String comments);
    }

    public static interface RatingStep {
        BuildStep withRating(Integer rating);
    }

    public static interface BuildStep {
        ReviewDto build();
    }

    public static class Builder
        implements ByUserIdStep, RideIdStep, SourceStep, DestinationStep, DateStep, TagsStep, CommentsStep, RatingStep,
        BuildStep {
        private Long byUserId;
        private String rideId;
        private String source;
        private String destination;
        private LocalDateTime date;
        private Set<ReviewTag> tags;
        private String comments;
        private Integer rating;

        private Builder() {
        }

        public static ByUserIdStep userReviewDto() {
            return new Builder();
        }

        @Override
        public RideIdStep withByUserId(Long byUserId) {
            this.byUserId = byUserId;
            return this;
        }

        @Override
        public SourceStep withRideId(String rideId) {
            this.rideId = rideId;
            return this;
        }

        @Override
        public DestinationStep withSource(String source) {
            this.source = source;
            return this;
        }

        @Override
        public DateStep withDestination(String destination) {
            this.destination = destination;
            return this;
        }

        @Override
        public TagsStep withDate(LocalDateTime date) {
            this.date = date;
            return this;
        }

        @Override
        public CommentsStep withTags(Set<ReviewTag> tags) {
            this.tags = tags;
            return this;
        }

        @Override
        public RatingStep withComments(String comments) {
            this.comments = comments;
            return this;
        }

        @Override
        public BuildStep withRating(Integer rating) {
            this.rating = rating;
            return this;
        }

        @Override
        public ReviewDto build() {
            return new ReviewDto(
                this.byUserId,
                this.rideId,
                this.source,
                this.destination,
                this.date,
                this.tags,
                this.comments,
                this.rating
            );
        }
    }
}
