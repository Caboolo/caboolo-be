package com.caboolo.backend.review.service;

import com.caboolo.backend.review.domain.Review;
import com.caboolo.backend.review.dto.*;
import com.caboolo.backend.review.enums.ReviewTagType;
import com.caboolo.backend.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;

    @Override
    @Transactional
    public void submitReview(SubmitReviewRequestDto request) {
        List<Review> reviews = request.getReviews().stream()
                .map(item -> Review.Builder.review()
                        .withRideId(request.getRideId())
                        .withForUserId(item.getToUserId())
                        .withByUserId(request.getByUserId())
                        .withRating(item.getRating())
                        .withComment(item.getComment())
                        .withRideAgain(item.getRideAgain())
                        .withTags(item.getTags())
                        .build())
                .collect(Collectors.toList());
        reviewRepository.saveAll(reviews);
    }

    @Override
    public CoPassengerResponseDto getListOfCoPassengers(Long rideId) {
        // Hardcoded ride details as requested
        return CoPassengerResponseDto.Builder.builder()
                .withFromLocation("Indira Nagar")
                .withToLocation("Electronic City")
                .withRiders(List.of(
                        RiderDto.Builder.builder()
                                .withUserId(101L)
                                .withName("Rahul Sharma")
                                .withAvgRating(4.5)
                                .build(),
                        RiderDto.Builder.builder()
                                .withUserId(102L)
                                .withName("Priya Singh")
                                .withAvgRating(4.8)
                                .build()
                ))
                .build();
    }

    @Override
    public ProfileHeaderDto getMyProfileHeader(Long userId) {
        List<Review> reviews = reviewRepository.findByForUserId(userId);
        
        Double avgRating = reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);

        Map<String, Integer> tagCountMap = new HashMap<>();
        reviews.forEach(r -> {
            if (r.getTags() != null) {
                r.getTags().forEach(tag -> tagCountMap.merge(tag.name(), 1, Integer::sum));
            }
        });

        Map<String, Integer> top5Tags = tagCountMap.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(5)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));

        return ProfileHeaderDto.Builder.builder()
                .withName("Current User") // Dummy name, usually fetched from user service
                .withAvgRating(avgRating)
                .withTagCountMap(top5Tags)
                .withNumberOfRides(reviews.size()) // Dummy: assuming each review is from a ride
                .withTotalReviews(reviews.size())
                .build();
    }

    @Override
    public List<ReviewMinDto> getMyProfileDetail(Long userId) {
        List<Review> reviews = reviewRepository.findByForUserId(userId);

        return reviews.stream()
                .map(r -> ReviewMinDto.Builder.builder()
                        .withByUserId(r.getByUserId())
                        .withFromToLocation("Point A to Point B") // Hardcoded as per requirement
                        .withDate(LocalDateTime.now()) // Hardcoded/Dummy
                        .withTags(r.getTags())
                        .withComments(r.getComment())
                        .withRating(r.getRating())
                        .build())
                .sorted(Comparator.comparing(ReviewMinDto::getRating).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public CoTravellerProfileDto getCoTravellerProfile(Long userId) {
        List<Review> reviews = reviewRepository.findByForUserId(userId);

        Double avgRating = reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);

        Map<String, Integer> tagCountMap = new HashMap<>();
        Map<Integer, Integer> ratingBreakdown = new HashMap<>();
        reviews.forEach(r -> {
            if (r.getTags() != null) {
                r.getTags().forEach(tag -> tagCountMap.merge(tag.name(), 1, Integer::sum));
            }
            ratingBreakdown.merge(r.getRating(), 1, Integer::sum);
        });

        return CoTravellerProfileDto.Builder.builder()
                .withName("Co-Traveller")
                .withNumberOfRides(reviews.size())
                .withAvgRating(avgRating)
                .withNoOfReviews(reviews.size())
                .withTrustScore(85.0) // Dummy
                .withTagCountMap(tagCountMap)
                .withRatingBreakdown(ratingBreakdown)
                .build();
    }
}
