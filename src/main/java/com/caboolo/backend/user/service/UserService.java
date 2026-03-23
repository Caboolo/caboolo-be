package com.caboolo.backend.user.service;

import com.caboolo.backend.dto.UserProfileRequest;
import com.caboolo.backend.dto.UserProfileResponse;
import com.caboolo.backend.storage.StorageService;
import com.caboolo.backend.storage.StorageUploadResult;
import com.caboolo.backend.user.domain.User;
import com.caboolo.backend.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private static final String PROFILE_PHOTO_FOLDER = "caboolo/profile_photos";
    private static final long MAX_PHOTO_SIZE_BYTES = 5 * 1024 * 1024; // 5 MB
    private static final List<String> ALLOWED_PHOTO_TYPES =
        List.of("image/jpeg", "image/png", "image/webp", "image/gif");

    private final UserRepository userRepository;
    private final StorageService storageService;

    public UserService(UserRepository userRepository, StorageService storageService) {
        this.userRepository = userRepository;
        this.storageService = storageService;
    }

    // -----------------------------------------------------------------------
    // Auth flow
    // -----------------------------------------------------------------------

    public User handleLogin(String uid, String phoneNumber) {
        Optional<User> existingUserOpt = userRepository.findByFirebaseUid(uid);
        User user;

        if (existingUserOpt.isEmpty()) {
            user = new User(uid, phoneNumber);
            user = userRepository.save(user);
        } else {
            user = existingUserOpt.get();
            // Update phone number if missing or changed
            if (phoneNumber != null && !phoneNumber.equals(user.getPhoneNumber())) {
                user.setPhoneNumber(phoneNumber);
                user = userRepository.save(user);
            }
        }
        return user;
    }

    // -----------------------------------------------------------------------
    // Profile management
    // -----------------------------------------------------------------------

    /**
     * Get the precise photo URL for a given user ID. 
     * Useful for unauthenticated resolve API endpoints.
     */
    public String getPhotoUrlByUserId(Long userId) {
        return userRepository.findById(userId)
            .map(User::getPhotoUrl)
            .orElseThrow(() -> new RuntimeException("User not found: " + userId));
    }

    /**
     * Fetch the profile for the authenticated user.
     */
    public UserProfileResponse getProfile(String firebaseUid) {
        User user = findActiveUserOrThrow(firebaseUid);
        return toResponse(user);
    }

    /**
     * Update display name and/or email.
     */
    public UserProfileResponse updateProfile(String firebaseUid, UserProfileRequest request) {
        User user = findActiveUserOrThrow(firebaseUid);

        if (request.getDisplayName() != null) {
            user.setDisplayName(request.getDisplayName());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }

        user = userRepository.save(user);
        return toResponse(user);
    }

    /**
     * Upload (or replace) the user's profile photo.
     * The previous photo is soft-replaced: the old file is deleted from the storage
     * provider but the user record itself is never hard-deleted.
     */
    public UserProfileResponse uploadProfilePhoto(String firebaseUid, MultipartFile file) {
        validatePhoto(file);

        User user = findActiveUserOrThrow(firebaseUid);

        // Delete old photo from provider if one exists
        if (user.getPhotoPublicId() != null && !user.getPhotoPublicId().isBlank()) {
            storageService.delete(user.getPhotoPublicId());
        }

        StorageUploadResult result = storageService.upload(file, PROFILE_PHOTO_FOLDER);

        user.setPhotoUrl(result.getUrl());
        user.setPhotoPublicId(result.getPublicId());
        user = userRepository.save(user);

        return toResponse(user);
    }

    /**
     * Soft-delete a user (isDeleted = true). The record stays in the DB.
     */
    public void softDeleteUser(String firebaseUid) {
        User user = findActiveUserOrThrow(firebaseUid);
        user.setDeleted(true);
        userRepository.save(user);
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

    private User findActiveUserOrThrow(String firebaseUid) {
        return userRepository.findByFirebaseUidAndIsDeletedFalse(firebaseUid)
            .orElseThrow(() -> new RuntimeException("User not found: " + firebaseUid));
    }

    private UserProfileResponse toResponse(User user) {
        UserProfileResponse resp = new UserProfileResponse();
        resp.setId(user.getId());
        resp.setFirebaseUid(user.getFirebaseUid());
        resp.setPhoneNumber(user.getPhoneNumber());
        resp.setDisplayName(user.getDisplayName());
        resp.setEmail(user.getEmail());
        resp.setPhotoUrl(user.getPhotoUrl());
        resp.setRole(user.getRole());
        resp.setDateCreated(user.getDateCreated());
        resp.setLastModified(user.getLastModified());
        return resp;
    }
}
