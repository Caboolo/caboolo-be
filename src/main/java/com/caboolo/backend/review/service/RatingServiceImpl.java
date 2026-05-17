package com.caboolo.backend.review.service;

import com.caboolo.backend.review.enums.ReviewTag;
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
    public Map<Integer, List<ReviewTag>> getRatingTagsGroupedByStars() {
        try {
            Object cachedData = redisTemplate.opsForValue().get(RATING_TAGS_CACHE_KEY);
            if (cachedData instanceof Map) {
                log.info("Returning rating tags from cache");
                return (Map<Integer, List<ReviewTag>>) cachedData;
            }
        } catch (Exception e) {
            log.error("Failed to fetch rating tags from Redis cache", e);
        }

        log.info("Fetching rating tags from source (fallback)");
        Map<Integer, List<ReviewTag>> tags = getDefaultTags();

        try {
            redisTemplate.opsForValue().set(RATING_TAGS_CACHE_KEY, tags, CACHE_DURATION);
            log.info("Cached rating tags in Redis");
        } catch (Exception e) {
            log.error("Failed to cache rating tags in Redis", e);
        }

        return tags;
    }

    private Map<Integer, List<ReviewTag>> getDefaultTags() {
        Map<Integer, List<ReviewTag>> tags = new HashMap<>();
        
        tags.put(5, Arrays.asList(
                ReviewTag.FRIENDLY, ReviewTag.GREAT_CONVERSATION, ReviewTag.PUNCTUAL, ReviewTag.RESPECTFUL, 
                ReviewTag.CLEAN_PASSENGER, ReviewTag.HELPFUL, ReviewTag.SMOOTH_RIDE, ReviewTag.COOPERATIVE, ReviewTag.RECOMMENDED
        ));
        
        tags.put(4, Arrays.asList(
                ReviewTag.POLITE, ReviewTag.MOSTLY_ON_TIME, ReviewTag.GOOD_BEHAVIOUR, 
                ReviewTag.COMFORTABLE_RIDE, ReviewTag.EASY_GOING, ReviewTag.RESPONSIVE
        ));
        
        tags.put(3, Arrays.asList(
                ReviewTag.AVERAGE_EXPERIENCE, ReviewTag.OKAY_RIDE, ReviewTag.COULD_IMPROVE_PUNCTUALITY, 
                ReviewTag.LESS_INTERACTIVE, ReviewTag.NEUTRAL_EXPERIENCE, ReviewTag.ACCEPTABLE_BEHAVIOUR
        ));
        
        tags.put(2, Arrays.asList(
                ReviewTag.LATE_ARRIVAL, ReviewTag.POOR_COMMUNICATION, ReviewTag.UNTIDY, 
                ReviewTag.UNCOMFORTABLE_RIDE, ReviewTag.DISTRACTING_BEHAVIOUR, ReviewTag.INCONSIDERATE
        ));
        
        tags.put(1, Arrays.asList(
                ReviewTag.VERY_LATE, ReviewTag.RUDE_BEHAVIOUR, ReviewTag.HARASSMENT, 
                ReviewTag.SMOKING_DURING_RIDE, ReviewTag.VEHICLE_UNCLEAN, ReviewTag.RECKLESS_BEHAVIOUR, 
                ReviewTag.ABUSIVE_LANGUAGE, ReviewTag.FELT_UNSAFE
        ));
        
        return tags;
    }
}
