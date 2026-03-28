package com.caboolo.backend.review.dto;

import com.caboolo.backend.review.enums.ReviewTagType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReviewMinDto {
    private String byUserId;
    private String fromToLocation;
    private LocalDateTime date;
    private Set<ReviewTagType> tags;
    private String comments;
    private Integer rating;

    public static interface ByUserIdStep {
        FromToLocationStep withByUserId(String byUserId);
    }

    public static interface FromToLocationStep {
        DateStep withFromToLocation(String fromToLocation);
    }

    public static interface DateStep {
        TagsStep withDate(LocalDateTime date);
    }

    public static interface TagsStep {
        CommentsStep withTags(Set<ReviewTagType> tags);
    }

    public static interface CommentsStep {
        RatingStep withComments(String comments);
    }

    public static interface RatingStep {
        BuildStep withRating(Integer rating);
    }

    public static interface BuildStep {
        ReviewMinDto build();
    }

    public static class Builder implements ByUserIdStep, FromToLocationStep, DateStep, TagsStep, CommentsStep, RatingStep, BuildStep {
        private String byUserId;
        private String fromToLocation;
        private LocalDateTime date;
        private Set<ReviewTagType> tags;
        private String comments;
        private Integer rating;

        public static ByUserIdStep builder() {
            return new Builder();
        }

        @Override
        public FromToLocationStep withByUserId(String byUserId) {
            this.byUserId = byUserId;
            return this;
        }

        @Override
        public DateStep withFromToLocation(String fromToLocation) {
            this.fromToLocation = fromToLocation;
            return this;
        }

        @Override
        public TagsStep withDate(LocalDateTime date) {
            this.date = date;
            return this;
        }

        @Override
        public CommentsStep withTags(Set<ReviewTagType> tags) {
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
        public ReviewMinDto build() {
            return new ReviewMinDto(byUserId, fromToLocation, date, tags, comments, rating);
        }
    }
}
