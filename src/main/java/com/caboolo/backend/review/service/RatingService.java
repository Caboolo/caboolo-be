package com.caboolo.backend.review.service;

import com.caboolo.backend.review.enums.ReviewTag;

import java.util.List;
import java.util.Map;

public interface RatingService {
    Map<Integer, List<ReviewTag>> getRatingTagsGroupedByStars();
}
