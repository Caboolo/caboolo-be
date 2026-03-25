package com.caboolo.backend.userdetails.service;

import com.caboolo.backend.dto.UserProfileRequestDto;
import com.caboolo.backend.dto.UserProfileResponseDto;
import com.caboolo.backend.storage.StorageService;
import com.caboolo.backend.storage.StorageUploadResult;
import com.caboolo.backend.userLogin.domain.UserLogin;
import com.caboolo.backend.userLogin.repository.UserLoginRepository;
import com.caboolo.backend.userdetails.converter.UserDetailsConverter;
import com.caboolo.backend.userdetails.domain.UserDetails;
import com.caboolo.backend.userdetails.dto.UserDetailRequestDto;
import com.caboolo.backend.userdetails.dto.UserDetailResponseDto;
import com.caboolo.backend.userdetails.repository.UserDetailRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@Service
public class UserDetailService {

    private static final String PROFILE_PHOTO_FOLDER = "caboolo/profile_photos";
    private static final long MAX_PHOTO_SIZE_BYTES = 5 * 1024 * 1024; // 5 MB
    private static final List<String> ALLOWED_PHOTO_TYPES =
            List.of("image/jpeg", "image/png", "image/webp", "image/gif");

    private final UserDetailRepository userDetailRepository;
    private final UserLoginRepository userLoginRepository;
    private final StorageService storageService;

    public UserDetailService(UserDetailRepository userDetailRepository,
                             UserLoginRepository userLoginRepository,
                             StorageService storageService) {
        this.userDetailRepository = userDetailRepository;
        this.userLoginRepository = userLoginRepository;
        this.storageService = storageService;
    }

    public UserDetailResponseDto saveOrUpdateUserDetails(UserDetailRequestDto requestDto) {
        if (requestDto.getUserId() == null) {
            throw new IllegalArgumentException("User ID must not be null");
        }

        Optional<UserDetails> existingDetailsOpt = userDetailRepository.findByUserId(requestDto.getUserId());
        UserDetails details;

        if (existingDetailsOpt.isPresent()) {
            details = existingDetailsOpt.get();
            details.setName(requestDto.getName());
            details.setGender(requestDto.getGender());
            details.setImageUrl(requestDto.getImageUrl());
            details.setEmail(requestDto.getEmail());
        } else {
            details = UserDetails.Builder.userDetails()
                    .withName(requestDto.getName())
                    .withUserId(requestDto.getUserId())
                    .withGender(requestDto.getGender())
                    .withImageUrl(requestDto.getImageUrl())
                    .withEmail(requestDto.getEmail())
                    .withPhoneNumber(null)
                    .withPhotoPublicId(null)
                    .build();
        }

        UserDetails saved = userDetailRepository.save(details);
        return UserDetailsConverter.toDetailResponseDto(saved);
    }

    // -----------------------------------------------------------------------
    // Profile management (moved from UserLoginService)
    // -----------------------------------------------------------------------

    /**
     * Get the precise photo URL for a given user ID.
     * Useful for unauthenticated resolve API endpoints.
     */
    public String getPhotoUrlByUserId(Long userId) {
        return userDetailRepository.findByUserId(userId)
                .map(UserDetails::getImageUrl)
                .orElseThrow(() -> new RuntimeException("User profile not found: " + userId));
    }

    /**
     * Fetch the profile for the authenticated user.
     */
    public UserProfileResponseDto getProfile(String firebaseUid) {
        UserLogin userLogin = findActiveUserOrThrow(firebaseUid);
        UserDetails details = userDetailRepository.findByUserId(userLogin.getId()).orElseGet(() ->
                UserDetails.Builder.userDetails()
                        .withName(null)
                        .withUserId(userLogin.getId())
                        .withGender(null)
                        .withImageUrl(null)
                        .withEmail(null)
                        .withPhoneNumber(null)
                        .withPhotoPublicId(null)
                        .build()
        );
        return UserDetailsConverter.toProfileResponse(userLogin, details);
    }

    /**
     * Update display name and/or email.
     */
    public UserProfileResponseDto updateProfile(String firebaseUid, UserProfileRequestDto request) {
        UserLogin userLogin = findActiveUserOrThrow(firebaseUid);
        UserDetails details = userDetailRepository.findByUserId(userLogin.getId())
                .orElseGet(() -> UserDetails.Builder.userDetails()
                        .withName(null)
                        .withUserId(userLogin.getId())
                        .withGender(null)
                        .withImageUrl(null)
                        .withEmail(null)
                        .withPhoneNumber(null)
                        .withPhotoPublicId(null)
                        .build());

        if (request.getName() != null) {
            details.setName(request.getName());
        }
        if (request.getEmail() != null) {
            details.setEmail(request.getEmail());
        }

        details = userDetailRepository.save(details);
        return UserDetailsConverter.toProfileResponse(userLogin, details);
    }

    /**
     * Upload (or replace) the user's profile photo.
     * The previous photo is soft-replaced: the old file is deleted from the storage
     * provider but the user record itself is never hard-deleted.
     */
    public UserProfileResponseDto uploadProfilePhoto(String firebaseUid, MultipartFile file) {
        validatePhoto(file);

        UserLogin userLogin = findActiveUserOrThrow(firebaseUid);
        UserDetails details = userDetailRepository.findByUserId(userLogin.getId())
                .orElseGet(() -> UserDetails.Builder.userDetails()
                        .withName(null)
                        .withUserId(userLogin.getId())
                        .withGender(null)
                        .withImageUrl(null)
                        .withEmail(null)
                        .withPhoneNumber(null)
                        .withPhotoPublicId(null)
                        .build());

        // Delete old photo from provider if one exists
        if (details.getPhotoPublicId() != null && !details.getPhotoPublicId().isBlank()) {
            storageService.delete(details.getPhotoPublicId());
        }

        StorageUploadResult result = storageService.upload(file, PROFILE_PHOTO_FOLDER);

        details.setImageUrl(result.getUrl());
        details.setPhotoPublicId(result.getPublicId());
        details = userDetailRepository.save(details);

        return UserDetailsConverter.toProfileResponse(userLogin, details);
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


}
