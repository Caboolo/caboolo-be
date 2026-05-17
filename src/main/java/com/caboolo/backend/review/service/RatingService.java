package com.caboolo.backend.review.service;

import java.util.List;
import java.util.Map;

public interface RatingService {
    Map<Integer, List<String>> getRatingTagsGroupedByStars();
}
