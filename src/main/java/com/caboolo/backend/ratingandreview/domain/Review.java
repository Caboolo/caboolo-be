package com.caboolo.backend.ratingandreview.domain;

import com.caboolo.backend.core.domain.GenericIdEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "reviews",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"ride_id", "reviewer_id", "reviewed_id"}
        )
)
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Review extends GenericIdEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

    @Column(name = "would_ride_again")
    private Boolean wouldRideAgain;


}
