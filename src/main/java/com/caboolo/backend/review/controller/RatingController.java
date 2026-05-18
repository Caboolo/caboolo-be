package com.caboolo.backend.review.controller;

import com.caboolo.backend.core.controller.BaseController;
import com.caboolo.backend.core.dto.RestEntity;
import com.caboolo.backend.review.service.RatingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/ratings")
@RequiredArgsConstructor
public class RatingController extends BaseController {

    private final RatingService ratingService;

    @GetMapping("/tags")
    public RestEntity<Map<Integer, List<String>>> getRatingTags() {
        return successResponse(ratingService.getRatingTagsGroupedByStars(), "Rating tags retrieved successfully");
    }
}
