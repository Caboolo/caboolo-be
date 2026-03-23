package com.caboolo.backend.userLogin.service;

import com.caboolo.backend.dto.UserProfileRequest;
import com.caboolo.backend.dto.UserProfileResponse;
import com.caboolo.backend.storage.StorageService;
import com.caboolo.backend.storage.StorageUploadResult;
import com.caboolo.backend.userLogin.domain.UserLogin;
import com.caboolo.backend.userLogin.repository.UserLoginRepository;
import com.caboolo.backend.userdetails.domain.UserDetails;
import com.caboolo.backend.userdetails.repository.UserDetailRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@Service
public class UserLoginService {

    private static final String PROFILE_PHOTO_FOLDER = "caboolo/profile_photos";
    private static final long MAX_PHOTO_SIZE_BYTES = 5 * 1024 * 1024; // 5 MB
    private static final List<String> ALLOWED_PHOTO_TYPES =
            List.of("image/jpeg", "image/png", "image/webp", "image/gif");

    private final UserLoginRepository userLoginRepository;
    private final UserDetailRepository userDetailRepository;
    private final StorageService storageService;

    public UserLoginService(UserLoginRepository userLoginRepository, UserDetailRepository userDetailRepository,
                            StorageService storageService) {
        this.userLoginRepository = userLoginRepository;
        this.userDetailRepository = userDetailRepository;
        this.storageService = storageService;
    }

    // -----------------------------------------------------------------------
    // Auth flow
    // -----------------------------------------------------------------------

    public UserLogin handleLogin(String uid, String phoneNumber) {
        Optional<UserLogin> existingUserOpt = userLoginRepository.findByFirebaseUid(uid);
        UserLogin userLogin;

        if (existingUserOpt.isEmpty()) {
            userLogin = new UserLogin(uid, phoneNumber);
            userLogin = userLoginRepository.save(userLogin);
        } else {
            userLogin = existingUserOpt.get();
            // Update phone number if missing or changed
            if (phoneNumber != null && !phoneNumber.equals(userLogin.getPhoneNumber())) {
                userLogin.setPhoneNumber(phoneNumber);
                userLogin = userLoginRepository.save(userLogin);
            }
        }
        return userLogin;
    }

    // -----------------------------------------------------------------------
    // Profile management
    // -----------------------------------------------------------------------

    /**
     * Get the precise photo URL for a given UserLogin ID.
     * Useful for unauthenticated resolve API endpoints.
     */
    public String getPhotoUrlByUserId(Long userId) {
        return userDetailRepository.findByUserId(userId)
                .map(UserDetails::getImageUrl)
                .orElseThrow(() -> new RuntimeException("UserLogin profile not found: " + userId));
    }

    /**
     * Fetch the profile for the authenticated UserLogin.
     */
    public UserProfileResponse getProfile(String firebaseUid) {
        UserLogin userLogin = findActiveUserOrThrow(firebaseUid);
        UserDetails details = userDetailRepository.findByUserId(userLogin.getId()).orElse(new UserDetails());
        return toResponse(userLogin, details);
    }

    /**
     * Update display name and/or email.
     */
    public UserProfileResponse updateProfile(String firebaseUid, UserProfileRequest request) {
        UserLogin userLogin = findActiveUserOrThrow(firebaseUid);
        UserDetails details = userDetailRepository.findByUserId(userLogin.getId())
                .orElse(new UserDetails(null, userLogin.getId(), null, null, null, null));

        if (request.getName() != null) {
            details.setName(request.getName());
        }
        if (request.getEmail() != null) {
            details.setEmail(request.getEmail());
        }

        details = userDetailRepository.save(details);
        return toResponse(userLogin, details);
    }

    /**
     * Upload (or replace) the UserLogin's profile photo.
     * The previous photo is soft-replaced: the old file is deleted from the storage
     * provider but the UserLogin record itself is never hard-deleted.
     */
    public UserProfileResponse uploadProfilePhoto(String firebaseUid, MultipartFile file) {
        validatePhoto(file);

        UserLogin userLogin = findActiveUserOrThrow(firebaseUid);
        UserDetails details = userDetailRepository.findByUserId(userLogin.getId())
                .orElse(new UserDetails(null, userLogin.getId(), null, null, null, null));

        // Delete old photo from provider if one exists
        if (details.getPhotoPublicId() != null && !details.getPhotoPublicId().isBlank()) {
            storageService.delete(details.getPhotoPublicId());
        }

        StorageUploadResult result = storageService.upload(file, PROFILE_PHOTO_FOLDER);

        details.setImageUrl(result.getUrl());
        details.setPhotoPublicId(result.getPublicId());
        details = userDetailRepository.save(details);

        return toResponse(userLogin, details);
    }

    /**
     * Soft-delete a UserLogin (isDeleted = true). The record stays in the DB.
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
                .orElseThrow(() -> new RuntimeException("UserLogin not found: " + firebaseUid));
    }

    private UserProfileResponse toResponse(UserLogin userLogin, UserDetails details) {
        UserProfileResponse resp = new UserProfileResponse();
        resp.setId(userLogin.getId());
        resp.setFirebaseUid(userLogin.getFirebaseUid());
        resp.setPhoneNumber(userLogin.getPhoneNumber());
        resp.setName(details.getName());
        resp.setEmail(details.getEmail());
        resp.setImageUrl(details.getImageUrl());
        resp.setDateCreated(userLogin.getDateCreated());
        resp.setLastModified(userLogin.getLastModified());
        return resp;
    }
}
