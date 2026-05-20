package com.caboolo.backend.review.service;

import com.caboolo.backend.core.idgen.SequenceGenerator;
import com.caboolo.backend.review.domain.Review;
import com.caboolo.backend.review.dto.*;
import com.caboolo.backend.review.repository.ReviewRepository;
import com.caboolo.backend.userdetails.domain.UserDetail;
import com.caboolo.backend.userdetails.service.UserDetailService;
import com.caboolo.backend.userdetails.dto.UserDetailResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final SequenceGenerator sequenceGenerator;
    private final UserDetailService userDetailService;

    public ReviewServiceImpl(ReviewRepository reviewRepository, SequenceGenerator sequenceGenerator,
                             UserDetailService userDetailService) {
        this.reviewRepository = reviewRepository;
        this.sequenceGenerator = sequenceGenerator;
        this.userDetailService = userDetailService;
    }

    @Override
    @Transactional
    public void submitReview(RideReviewResponseDto request) {
        log.info("Submitting {} review(s) for rideId={}, byUserId={}",
                request.getReviews().size(), request.getRideId(), request.getByUserId());
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
        log.info("Saved {} review(s) for rideId={}", reviews.size(), request.getRideId());

        // Update user statistics asynchronously
        request.getReviews().stream()
                .map(UserReviewDto::getToUserId)
                .distinct()
                .forEach(userDetailService::updateUserStatsAsync);
    }

    @Override
    public RideReviewRequestDto getListOfCoPassengers(String rideId, String byUserId) {
        log.info("Fetching co-passenger listing for rideId={}, byUserId={}", rideId, byUserId);

        // Fetch reviews already submitted by this user for this ride and index by forUserId
        Map<String, Review> existingReviewMap = reviewRepository
                .findByRideIdAndByUserId(rideId, byUserId)
                .stream()
                .collect(Collectors.toMap(Review::getForUserId, r -> r));

        // TODO: replace with real co-passenger fetch from ride participant service
        // For now we derive the list from the users who have been reviewed or passed in
        // This is a placeholder list — replace with actual ride participant lookup
        List<String> coPassengerUserIds = new ArrayList<>(existingReviewMap.keySet());

        List<UserReviewDto> riders = coPassengerUserIds.stream()
                .map(toUserId -> {
                    UserDetail userDetail = userDetailService.getUserDetailEntity(toUserId);

                    MinProfileDto profile = MinProfileDto.Builder.minProfileDto()
                            .withUserId(toUserId)
                            .withName(userDetail.getName())
                            .withAvgRating(userDetail.getAvgRating() != null ? userDetail.getAvgRating() : 0.0)
                            .build();

                    Review existing = existingReviewMap.get(toUserId);
                    if (existing != null) {
                        // Already rated — prefill all fields
                        return UserReviewDto.Builder.builder()
                                .withToUserId(toUserId)
                                .withToUserProfile(profile)
                                .withRating(existing.getRating())
                                .withRideAgain(existing.getRideAgain())
                                .withTags(existing.getTags())
                                .withComment(existing.getComment())
                                .build();
                    } else {
                        // Not yet rated — return only identity + profile, rest null
                        return UserReviewDto.Builder.builder()
                                .withToUserId(toUserId)
                                .withToUserProfile(profile)
                                .withRating(null)
                                .withRideAgain(null)
                                .withTags(null)
                                .withComment(null)
                                .build();
                    }
                })
                .collect(Collectors.toList());

        // TODO: replace hardcoded source/destination with real ride lookup
        return RideReviewRequestDto.Builder.rideReviewRequestDto()
                .withRideId(rideId)
                .withSource(null)
                .withDestination(null)
                .withRiders(riders)
                .build();
    }

    @Override
    public List<ReviewDto> getReviewDtoList(String userId) {
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
        log.info("Fetching co-traveller profile for userId={}", userId);
        UserDetail userDetails = userDetailService.getUserDetailEntity(userId);

        Map<String, Integer> tagCountMap = userDetails.getTagCounts();
        if (tagCountMap == null) {
            tagCountMap = new HashMap<>();
        }
        
        // We still need rating breakdown, so we fetch reviews for that
        // OR we could store rating breakdown in UserDetails as well.
        // For now, let's keep review fetch for rating breakdown if it's not in UserDetails.
        List<Review> reviews = reviewRepository.findByForUserId(userId);
        Map<Integer, Integer> ratingBreakdown = new HashMap<>();
        reviews.forEach(r -> ratingBreakdown.merge(r.getRating(), 1, Integer::sum));

        return RiderProfileDto.Builder.riderProfileDto()
                .withUserId(userId)
                .withName(userDetails.getName())
                .withNumberOfRides(userDetails.getTotalReviews())
                .withAvgRating(userDetails.getAvgRating() != null ? userDetails.getAvgRating() : 0.0)
                .withNoOfReviews(userDetails.getTotalReviews() != null ? userDetails.getTotalReviews() : 0)
                .withTagCountMap(tagCountMap)
                .withTrustScore(
                        userDetails.getTotalReviews() == 0
                                ? 0
                                : (userDetails.getRideAgainCount() * 100.0) / userDetails.getTotalReviews()
                )
                .withRatingBreakdown(ratingBreakdown)
                .build();
    }
}
