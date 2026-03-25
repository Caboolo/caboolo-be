package com.caboolo.backend.ratingandreview.domain;

import com.caboolo.backend.core.domain.GenericIdEntity;
import com.caboolo.backend.ratingandreview.enums.ReviewTagType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "review_tag")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ReviewTag extends GenericIdEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "review_id", nullable = false)
    private Long reviewId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tag", nullable = false)
    private ReviewTagType tag;
}