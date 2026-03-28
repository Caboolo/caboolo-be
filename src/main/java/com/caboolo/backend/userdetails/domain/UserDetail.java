package com.caboolo.backend.userdetails.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;
import com.caboolo.backend.core.domain.GenericIdEntity;

import java.util.Map;

@Entity
@Table(name = "user_detail")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UserDetail extends GenericIdEntity {

    @Column(name = "user_details_id")
    private Long userDetailsId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "user_id", nullable = false, unique = true)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 20)
    private Gender gender;

    @Column(name = "image_url", length = 1000)
    private String imageUrl;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "phone_number", unique = true)
    private String phoneNumber;

    @Column(name = "photo_public_id")
    private String photoPublicId;

    @Column(name = "avg_rating")
    private Double avgRating;

    @Column(name = "total_reviews")
    private Integer totalReviews;

    @Column(name = "ride_again_count")
    private Integer rideAgainCount;

    @Convert(converter = com.caboolo.backend.core.converter.JsonToMapConverter.class)
    @Column(name = "tag_counts", columnDefinition = "JSON")
    private Map<String, Integer> tagCounts;

    public static interface UserDetailsIdStep {
        NameStep withUserDetailsId(Long userDetailsId);
    }

    public static interface NameStep {
        UserIdStep withName(String name);
    }

    public static interface UserIdStep {
        GenderStep withUserId(String userId);
    }

    public static interface GenderStep {
        ImageUrlStep withGender(Gender gender);
    }

    public static interface ImageUrlStep {
        EmailStep withImageUrl(String imageUrl);
    }

    public static interface EmailStep {
        PhoneNumberStep withEmail(String email);
    }

    public static interface PhoneNumberStep {
        PhotoPublicIdStep withPhoneNumber(String phoneNumber);
    }

    public static interface PhotoPublicIdStep {
        AvgRatingStep withPhotoPublicId(String photoPublicId);
    }

    public static interface AvgRatingStep {
        TotalReviewsStep withAvgRating(Double avgRating);
    }

    public static interface TotalReviewsStep {
        RideAgainCountStep withTotalReviews(Integer totalReviews);
    }

    public static interface RideAgainCountStep {
        TagCountsStep withRideAgainCount(Integer rideAgainCount);
    }

    public static interface TagCountsStep {
        BuildStep withTagCounts(Map<String, Integer> tagCounts);
    }

    public static interface BuildStep {
        UserDetail build();
    }


    public static class Builder implements UserDetailsIdStep, NameStep, UserIdStep, GenderStep, ImageUrlStep, EmailStep, PhoneNumberStep, PhotoPublicIdStep, AvgRatingStep, TotalReviewsStep, RideAgainCountStep, TagCountsStep, BuildStep {
        private Long userDetailsId;
        private String name;
        private String userId;
        private Gender gender;
        private String imageUrl;
        private String email;
        private String phoneNumber;
        private String photoPublicId;
        private Double avgRating;
        private Integer totalReviews;
        private Integer rideAgainCount;
        private Map<String, Integer> tagCounts;

        private Builder() {
        }

        public static UserDetailsIdStep userDetails() {
            return new Builder();
        }

        @Override
        public NameStep withUserDetailsId(Long userDetailsId) {
            this.userDetailsId = userDetailsId;
            return this;
        }

        @Override
        public UserIdStep withName(String name) {
            this.name = name;
            return this;
        }

        @Override
        public GenderStep withUserId(String userId) {
            this.userId = userId;
            return this;
        }

        @Override
        public ImageUrlStep withGender(Gender gender) {
            this.gender = gender;
            return this;
        }

        @Override
        public EmailStep withImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
            return this;
        }

        @Override
        public PhoneNumberStep withEmail(String email) {
            this.email = email;
            return this;
        }

        @Override
        public PhotoPublicIdStep withPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
            return this;
        }

        @Override
        public AvgRatingStep withPhotoPublicId(String photoPublicId) {
            this.photoPublicId = photoPublicId;
            return this;
        }

        @Override
        public TotalReviewsStep withAvgRating(Double avgRating) {
            this.avgRating = avgRating;
            return this;
        }

        @Override
        public RideAgainCountStep withTotalReviews(Integer totalReviews) {
            this.totalReviews = totalReviews;
            return this;
        }

        @Override
        public TagCountsStep withRideAgainCount(Integer rideAgainCount) {
            this.rideAgainCount = rideAgainCount;
            return this;
        }

        @Override
        public BuildStep withTagCounts(Map<String, Integer> tagCounts) {
            this.tagCounts = tagCounts;
            return this;
        }

        @Override
        public UserDetail build() {
            return new UserDetail(
                    this.userDetailsId,
                    this.name,
                    this.userId,
                    this.gender,
                    this.imageUrl,
                    this.email,
                    this.phoneNumber,
                    this.photoPublicId,
                    this.avgRating,
                    this.totalReviews,
                    this.rideAgainCount,
                    this.tagCounts
            );
        }
    }
}
