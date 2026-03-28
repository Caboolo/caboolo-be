package com.caboolo.backend.review.dto;

import com.caboolo.backend.core.dto.GenericEntityDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfileDto extends GenericEntityDto {
    private String userId;
    private String name;
    private Integer numberOfRides;
    private Double avgRating;
    private Integer noOfReviews;
    private Map<String, Integer> tagCountMap;

    public ProfileDto(LocalDateTime dateCreated, LocalDateTime lastModified, boolean isDeleted,
                      String userId, String name, Integer numberOfRides, Double avgRating, Integer noOfReviews,
                      Map<String, Integer> tagCountMap) {
        super(dateCreated, lastModified, isDeleted);
        this.userId = userId;
        this.name = name;
        this.numberOfRides = numberOfRides;
        this.avgRating = avgRating;
        this.noOfReviews = noOfReviews;
        this.tagCountMap = tagCountMap;
    }

    public static interface DateCreatedStep {
        LastModifiedStep withDateCreated(LocalDateTime dateCreated);
    }

    public static interface LastModifiedStep {
        IsDeletedStep withLastModified(LocalDateTime lastModified);
    }

    public static interface IsDeletedStep {
        UserIdStep withIsDeleted(boolean isDeleted);
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
        BuildStep withTagCountMap(Map<String, Integer> tagCountMap);
    }

    public static interface BuildStep {
        ProfileDto build();
    }

    public static class Builder
        implements DateCreatedStep, LastModifiedStep, IsDeletedStep, UserIdStep, NameStep, NumberOfRidesStep,
        AvgRatingStep, NoOfReviewsStep, TagCountMapStep, BuildStep {
        private LocalDateTime dateCreated;
        private LocalDateTime lastModified;
        private boolean isDeleted;
        private String userId;
        private String name;
        private Integer numberOfRides;
        private Double avgRating;
        private Integer noOfReviews;
        private Map<String, Integer> tagCountMap;

        private Builder() {
        }

        public static DateCreatedStep profileDto() {
            return new Builder();
        }

        @Override
        public LastModifiedStep withDateCreated(LocalDateTime dateCreated) {
            this.dateCreated = dateCreated;
            return this;
        }

        @Override
        public IsDeletedStep withLastModified(LocalDateTime lastModified) {
            this.lastModified = lastModified;
            return this;
        }

        @Override
        public UserIdStep withIsDeleted(boolean isDeleted) {
            this.isDeleted = isDeleted;
            return this;
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
                this.dateCreated,
                this.lastModified,
                this.isDeleted,
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
