package com.caboolo.backend.ratingandreview.domain;

import com.caboolo.backend.core.domain.GenericIdEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "user_review_stats")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UserReviewStats extends GenericIdEntity {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "avg_rating")
    private Double avgRating;

    @Column(name = "total_reviews")
    private Integer totalReviews;

    @Column(name = "ride_again_yes_count")
    private Integer rideAgainYesCount;

    @Column(name = "ride_again_no_count")
    private Integer rideAgainNoCount;
}