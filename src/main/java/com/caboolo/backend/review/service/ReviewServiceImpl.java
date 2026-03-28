package com.caboolo.backend.review.service;

import com.caboolo.backend.review.domain.Review;
import com.caboolo.backend.review.dto.*;
import com.caboolo.backend.review.repository.ReviewRepository;
import com.caboolo.backend.userdetails.service.UserDetailService;
import com.caboolo.backend.userdetails.dto.UserDetailResponseDto;
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
    private final com.caboolo.backend.core.idgen.SequenceGenerator sequenceGenerator;
    private final UserDetailService userDetailService;

    @Override
    @Transactional
    public void submitReview(RideReviewResponseDto request) {
        List<Review> reviews = request.getReviews().stream()
                .map(item -> Review.Builder.review()
                        .withReviewId(sequenceGenerator.nextId())
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
    public RideReviewRequestDto getListOfCoPassengers(Long rideId) {
        // Hardcoded ride details as requested
        return RideReviewRequestDto.Builder.rideReviewRequestDto()
                .withRideId(1231L)
                .withSource("Indira Nagar")
                .withDestination("Electronic City")
                .withRiders(List.of(
                        MinProfileDto.Builder.minProfileDto()
                                .withUserId("user101")
                                .withName("Rahul Sharma")
                                .withAvgRating(4.5)
                                .build(),
                        MinProfileDto.Builder.minProfileDto()
                                .withUserId("user102")
                                .withName("Priya Singh")
                                .withAvgRating(4.8)
                                .build()
                ))
                .build();
    }

    @Override
    public ProfileDto getMyProfileHeader(String userId) {
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

        UserDetailResponseDto userDetails = userDetailService.getUserDetailsById(userId);

        return ProfileDto.Builder.profileDto()
                .withUserId(String.valueOf(userId))
                .withName(userDetails.getName())
                .withNumberOfRides(reviews.size()) // Dummy: assuming each review is from a ride
                .withAvgRating(avgRating)
                .withNoOfReviews(reviews.size())
                .withTagCountMap(top5Tags)
                .build();
    }

    @Override
    public List<ReviewDto> getMyProfileDetail(String userId) {
        List<Review> reviews = reviewRepository.findByForUserId(userId);

        return reviews.stream()
                .map(r -> ReviewDto.Builder.userReviewDto()
                        .withByUserId(r.getByUserId())
                        .withRideId(String.valueOf(r.getRideId()))
                        .withSource(null)
                        .withDestination(null)
                        .withDate(null)
                        .withTags(r.getTags())
                        .withComments(r.getComment())
                        .withRating(r.getRating())
                        .build())
                .sorted(Comparator.comparing(ReviewDto::getRating).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public RiderProfileDto getCoTravellerProfile(String userId) {
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

        UserDetailResponseDto userDetails = userDetailService.getUserDetailsById(userId);

        return RiderProfileDto.Builder.riderProfileDto()
                .withUserId(String.valueOf(userId))
                .withName(userDetails.getName())
                .withNumberOfRides(reviews.size())
                .withAvgRating(avgRating)
                .withNoOfReviews(reviews.size())
                .withTagCountMap(tagCountMap)
                .withTrustScore(null) // Dummy
                .withRatingBreakdown(ratingBreakdown)
                .build();
    }
}
