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
                ReviewTag.FRIENDLY.getDisplayName(), ReviewTag.GREAT_CONVERSATION.getDisplayName(), ReviewTag.PUNCTUAL.getDisplayName(), ReviewTag.RESPECTFUL.getDisplayName(), 
                ReviewTag.CLEAN_PASSENGER.getDisplayName(), ReviewTag.HELPFUL.getDisplayName(), ReviewTag.SMOOTH_RIDE.getDisplayName(), ReviewTag.COOPERATIVE.getDisplayName(), ReviewTag.RECOMMENDED.getDisplayName()
        ));
        
        tags.put(4, Arrays.asList(
                ReviewTag.POLITE.getDisplayName(), ReviewTag.MOSTLY_ON_TIME.getDisplayName(), ReviewTag.GOOD_BEHAVIOUR.getDisplayName(), 
                ReviewTag.COMFORTABLE_RIDE.getDisplayName(), ReviewTag.EASY_GOING.getDisplayName(), ReviewTag.RESPONSIVE.getDisplayName()
        ));
        
        tags.put(3, Arrays.asList(
                ReviewTag.AVERAGE_EXPERIENCE.getDisplayName(), ReviewTag.OKAY_RIDE.getDisplayName(), ReviewTag.COULD_IMPROVE_PUNCTUALITY.getDisplayName(), 
                ReviewTag.LESS_INTERACTIVE.getDisplayName(), ReviewTag.NEUTRAL_EXPERIENCE.getDisplayName(), ReviewTag.ACCEPTABLE_BEHAVIOUR.getDisplayName()
        ));
        
        tags.put(2, Arrays.asList(
                ReviewTag.LATE_ARRIVAL.getDisplayName(), ReviewTag.POOR_COMMUNICATION.getDisplayName(), ReviewTag.UNTIDY.getDisplayName(), 
                ReviewTag.UNCOMFORTABLE_RIDE.getDisplayName(), ReviewTag.DISTRACTING_BEHAVIOUR.getDisplayName(), ReviewTag.INCONSIDERATE.getDisplayName()
        ));
        
        tags.put(1, Arrays.asList(
                ReviewTag.VERY_LATE.getDisplayName(), ReviewTag.RUDE_BEHAVIOUR.getDisplayName(), ReviewTag.HARASSMENT.getDisplayName(), 
                ReviewTag.SMOKING_DURING_RIDE.getDisplayName(), ReviewTag.VEHICLE_UNCLEAN.getDisplayName(), ReviewTag.RECKLESS_BEHAVIOUR.getDisplayName(), 
                ReviewTag.ABUSIVE_LANGUAGE.getDisplayName(), ReviewTag.FELT_UNSAFE.getDisplayName()
        ));
        
        return tags;
    }
}
