package com.caboolo.backend.review.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class RatingServiceImpl implements RatingService {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String RATING_TAGS_CACHE_KEY = "rating:tags:grouped";
    private static final Duration CACHE_DURATION = Duration.ofDays(7);

    @Override
    @SuppressWarnings("unchecked")
    public Map<Integer, List<String>> getRatingTagsGroupedByStars() {
        try {
            Object cachedData = redisTemplate.opsForValue().get(RATING_TAGS_CACHE_KEY);
            if (cachedData instanceof Map) {
                log.info("Returning rating tags from cache");
                return (Map<Integer, List<String>>) cachedData;
            }
        } catch (Exception e) {
            log.error("Failed to fetch rating tags from Redis cache", e);
        }

        log.info("Fetching rating tags from source (fallback)");
        Map<Integer, List<String>> tags = getDefaultTags();

        try {
            redisTemplate.opsForValue().set(RATING_TAGS_CACHE_KEY, tags, CACHE_DURATION);
            log.info("Cached rating tags in Redis");
        } catch (Exception e) {
            log.error("Failed to cache rating tags in Redis", e);
        }

        return tags;
    }

    private Map<Integer, List<String>> getDefaultTags() {
        Map<Integer, List<String>> tags = new HashMap<>();
        
        tags.put(5, Arrays.asList(
                "Friendly", "Great Conversation", "Punctual", "Respectful", 
                "Clean Passenger", "Helpful", "Smooth Ride", "Cooperative", "Recommended"
        ));
        
        tags.put(4, Arrays.asList(
                "Polite", "Mostly On Time", "Good Behaviour", 
                "Comfortable Ride", "Easy Going", "Responsive"
        ));
        
        tags.put(3, Arrays.asList(
                "Average Experience", "Okay Ride", "Could Improve Punctuality", 
                "Less Interactive", "Neutral Experience", "Acceptable Behaviour"
        ));
        
        tags.put(2, Arrays.asList(
                "Late Arrival", "Poor Communication", "Untidy", 
                "Uncomfortable Ride", "Distracting Behaviour", "Inconsiderate"
        ));
        
        tags.put(1, Arrays.asList(
                "Very Late", "Rude Behaviour", "Harassment", 
                "Smoking During Ride", "Vehicle Unclean", "Reckless Behaviour", 
                "Abusive Language", "Felt Unsafe"
        ));
        
        return tags;
    }
}
