package com.caboolo.backend.userdetails.service;

import com.caboolo.backend.core.idgen.SequenceGenerator;
import com.caboolo.backend.dto.UserDetailRequestDto;
import com.caboolo.backend.review.dto.ProfileDto;
import com.caboolo.backend.review.dto.ReviewDto;
import com.caboolo.backend.review.enums.ReviewTag;
import com.caboolo.backend.storage.StorageService;
import com.caboolo.backend.storage.StorageUploadResult;
import com.caboolo.backend.userLogin.domain.UserLogin;
import com.caboolo.backend.userLogin.repository.UserLoginRepository;
import com.caboolo.backend.review.domain.Review;
import com.caboolo.backend.review.repository.ReviewRepository;
import com.caboolo.backend.userdetails.converter.UserDetailsConverter;
import com.caboolo.backend.userdetails.domain.UserDetail;
import com.caboolo.backend.userdetails.dto.UserDetailResponseDto;
import com.caboolo.backend.userdetails.repository.UserDetailRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserDetailService {

    private static final String PROFILE_PHOTO_FOLDER = "caboolo/profile_photos";
    private static final long MAX_PHOTO_SIZE_BYTES = 5 * 1024 * 1024; // 5 MB
    private static final List<String> ALLOWED_PHOTO_TYPES =
        List.of("image/jpeg", "image/png", "image/webp", "image/gif");

    private final UserDetailRepository userDetailRepository;
    private final UserLoginRepository userLoginRepository;
    private final StorageService storageService;
    private final com.caboolo.backend.core.idgen.SequenceGenerator sequenceGenerator;
    private final UserDetailsConverter userDetailsConverter;
    private final ReviewRepository reviewRepository;

    public UserDetailService(UserDetailRepository userDetailRepository, UserLoginRepository userLoginRepository,
                             StorageService storageService, SequenceGenerator sequenceGenerator,
                             UserDetailsConverter userDetailsConverter,
                             ReviewRepository reviewRepository) {
        this.userDetailRepository = userDetailRepository;
        this.userLoginRepository = userLoginRepository;
        this.storageService = storageService;
        this.sequenceGenerator = sequenceGenerator;
        this.userDetailsConverter = userDetailsConverter;
        this.reviewRepository = reviewRepository;
    }

    public UserDetailResponseDto createOrUpdateUserDetails(UserDetailRequestDto requestDto) {
        if (requestDto.getUserId() == null) {
            throw new IllegalArgumentException("User ID must not be null");
        }

        Optional<UserDetail> existingDetailsOpt = userDetailRepository.findByUserId(requestDto.getUserId());
        UserDetail details;

        if (existingDetailsOpt.isPresent()) {
            details = existingDetailsOpt.get();
            details.setName(requestDto.getName());
            details.setGender(requestDto.getGender());
            details.setImageUrl(requestDto.getImageUrl());
            details.setEmail(requestDto.getEmail());
        } else {
            details = UserDetail.Builder.userDetails()
                .withUserDetailsId(sequenceGenerator.nextId())
                .withName(requestDto.getName())
                .withUserId(requestDto.getUserId())
                .withGender(requestDto.getGender())
                .withImageUrl(requestDto.getImageUrl())
                .withEmail(requestDto.getEmail())
                .withPhoneNumber(requestDto.getPhoneNumber())
                .withPhotoPublicId(null)
                .withAvgRating(null)
                .withTotalReviews(null)
                .withRideAgainCount(null)
                .withTagCounts(null)
                .build();
        }

        UserDetail saved = userDetailRepository.save(details);
        return userDetailsConverter.toDetailResponseDto(saved);
    }

    // -----------------------------------------------------------------------
    // Profile management (moved from UserLoginService)
    // -----------------------------------------------------------------------

    /**
     * Get the precise photo URL for a given user ID.
     * Useful for unauthenticated resolve API endpoints.
     */
    public String getPhotoUrlByUserId(String userId) {
        return userDetailRepository.findByUserId(userId)
            .map(UserDetail::getImageUrl)
            .orElseThrow(() -> new RuntimeException("User profile not found: " + userId));
    }

    /**
     * Get the details for a given user ID.
     */
    public UserDetailResponseDto getUserDetailById(String userId) {
        return userDetailRepository.findByUserId(userId)
            .map(userDetailsConverter::toDetailResponseDto)
            .orElseThrow(() -> new RuntimeException("User profile not found: " + userId));
    }

    /**
     * Get the full UserDetail entity for internal service use.
     */
    public UserDetail getUserDetailEntity(String userId) {
        return userDetailRepository.findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("User profile not found: " + userId));
    }

    /**
     * Fetch the profile for the authenticated user.
     */
    public UserDetailResponseDto getProfile(String firebaseUid) {
        UserDetail details = userDetailRepository.findByUserId(firebaseUid).orElseGet(() ->
            UserDetail.Builder.userDetails()
                .withUserDetailsId(sequenceGenerator.nextId())
                .withName(null)
                .withUserId(firebaseUid)
                .withGender(null)
                .withImageUrl(null)
                .withEmail(null)
                .withPhoneNumber(null)
                .withPhotoPublicId(null)
                .withAvgRating(null)
                .withTotalReviews(null)
                .withRideAgainCount(null)
                .withTagCounts(null)
                .build()
        );
        return userDetailsConverter.toDetailResponseDto(details);
    }

    /**
     * Update display name and/or email.
     */
    public UserDetailResponseDto updateProfile(String firebaseUid, UserDetailRequestDto request) {
        UserDetail details = userDetailRepository.findByUserId(firebaseUid)
            .orElseGet(() -> UserDetail.Builder.userDetails()
                .withUserDetailsId(sequenceGenerator.nextId())
                .withName(null)
                .withUserId(firebaseUid)
                .withGender(null)
                .withImageUrl(null)
                .withEmail(null)
                .withPhoneNumber(null)
                .withPhotoPublicId(null)
                .withAvgRating(null)
                .withTotalReviews(null)
                .withRideAgainCount(null)
                .withTagCounts(null)
                .build());

        if (request.getName() != null) {
            details.setName(request.getName());
        }
        if (request.getEmail() != null) {
            details.setEmail(request.getEmail());
        }

        details = userDetailRepository.save(details);
        return userDetailsConverter.toDetailResponseDto(details);
    }

    /**
     * Upload (or replace) the user's profile photo.
     * The previous photo is soft-replaced: the old file is deleted from the storage
     * provider but the user record itself is never hard-deleted.
     */
    public UserDetailResponseDto uploadProfilePhoto(String firebaseUid, MultipartFile file) {
        validatePhoto(file);

        UserDetail details = userDetailRepository.findByUserId(firebaseUid)
            .orElseGet(() -> UserDetail.Builder.userDetails()
                .withUserDetailsId(sequenceGenerator.nextId())
                .withName(null)
                .withUserId(firebaseUid)
                .withGender(null)
                .withImageUrl(null)
                .withEmail(null)
                .withPhoneNumber(null)
                .withPhotoPublicId(null)
                .withAvgRating(null)
                .withTotalReviews(null)
                .withRideAgainCount(null)
                .withTagCounts(null)
                .build());

        // Delete old photo from provider if one exists
        if (details.getPhotoPublicId() != null && !details.getPhotoPublicId().isBlank()) {
            storageService.delete(details.getPhotoPublicId());
        }

        StorageUploadResult result = storageService.upload(file, PROFILE_PHOTO_FOLDER);

        details.setImageUrl(result.getUrl());
        details.setPhotoPublicId(result.getPublicId());
        details = userDetailRepository.save(details);

        return userDetailsConverter.toDetailResponseDto(details);
    }

    /**
     * Soft-delete a user (isDeleted = true). The record stays in the DB.
     */
    public void softDeleteUser(String firebaseUid) {
        UserLogin userLogin = findActiveUserOrThrow(firebaseUid);
        userLogin.setDeleted(true);
        userLoginRepository.save(userLogin);
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private void validatePhoto(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Photo file must not be empty.");
        }
        if (file.getSize() > MAX_PHOTO_SIZE_BYTES) {
            throw new IllegalArgumentException(
                "Photo exceeds the maximum allowed size of 5 MB.");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_PHOTO_TYPES.contains(contentType)) {
            throw new IllegalArgumentException(
                "Unsupported file type '" + contentType + "'. Allowed types: " + ALLOWED_PHOTO_TYPES);
        }
    }

    private UserLogin findActiveUserOrThrow(String firebaseUid) {
        return userLoginRepository.findByFirebaseUidAndIsDeletedFalse(firebaseUid)
            .orElseThrow(() -> new RuntimeException("User not found: " + firebaseUid));
    }

    public ProfileDto getMyProfileHeader(String userId) {
        UserDetail userDetails = getUserDetailEntity(userId);

        Map<String, Integer> tagCountMap = userDetails.getTagCounts();
        if (tagCountMap == null) {
            tagCountMap = new HashMap<>();
        }

        Map<String, Integer> top5Tags = tagCountMap.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .limit(5)
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (e1, e2) -> e1,
                LinkedHashMap::new
            ));

        return ProfileDto.Builder.profileDto()
            .withDateCreated(userDetails.getDateCreated())
            .withLastModified(userDetails.getLastModified())
            .withIsDeleted(userDetails.isDeleted())
            .withUserId(userId)
            .withName(userDetails.getName())
            .withNumberOfRides(userDetails.getTotalReviews()) // Using totalReviews as a proxy for rides
            .withAvgRating(userDetails.getAvgRating() != null ? userDetails.getAvgRating() : 0.0)
            .withNoOfReviews(userDetails.getTotalReviews() != null ? userDetails.getTotalReviews() : 0)
            .withTagCountMap(top5Tags)
            .build();
    }

    @Async
    public void updateUserStatsAsync(String userId) {
        Optional<UserDetail> detailsOpt = userDetailRepository.findByUserId(userId);
        if (detailsOpt.isEmpty()) return;

        UserDetail details = detailsOpt.get();
        List<Review> reviews = reviewRepository.findByForUserId(userId);

        if (reviews.isEmpty()) {
            details.setAvgRating(0.0);
            details.setTotalReviews(0);
            details.setRideAgainCount(0);
            details.setTagCounts(new HashMap<>());
        } else {
            double totalRating = 0;
            int rideAgainCount = 0;
            Map<String, Integer> tagCounts = new HashMap<>();

            for (Review review : reviews) {
                totalRating += review.getRating();
                if (Boolean.TRUE.equals(review.getRideAgain())) {
                    rideAgainCount++;
                }
                if (review.getTags() != null) {
                    for (ReviewTag tag : review.getTags()) {
                        tagCounts.merge(tag.name(), 1, Integer::sum);
                    }
                }
            }

            details.setAvgRating(totalRating / reviews.size());
            details.setTotalReviews(reviews.size());
            details.setRideAgainCount(rideAgainCount);
            details.setTagCounts(tagCounts);
        }

        userDetailRepository.save(details);
    }


    public List<UserDetail> findAllByUserIdIn(Set<String> participantUserIds) {
        return userDetailRepository.findAllByUserIdIn(participantUserIds);
    }
}
