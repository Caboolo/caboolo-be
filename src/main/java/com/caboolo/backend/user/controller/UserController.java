package com.caboolo.backend.user.controller;

import com.caboolo.backend.dto.UserProfileRequest;
import com.caboolo.backend.dto.UserProfileResponse;
import com.caboolo.backend.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * GET /api/v1/users/me
     * Returns the authenticated user's full profile.
     */
    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getProfile(
            @AuthenticationPrincipal String firebaseUid) {
        return ResponseEntity.ok(userService.getProfile(firebaseUid));
    }

    /**
     * PUT /api/v1/users/me
     * Updates display name and/or email.
     * Body: { "displayName": "...", "email": "..." }
     */
    @PutMapping("/me")
    public ResponseEntity<UserProfileResponse> updateProfile(
            @AuthenticationPrincipal String firebaseUid,
            @RequestBody UserProfileRequest request) {
        return ResponseEntity.ok(userService.updateProfile(firebaseUid, request));
    }

    /**
     * POST /api/v1/users/me/photo
     * Uploads (or replaces) the authenticated user's profile photo.
     * Content-Type: multipart/form-data  —  field name: "file"
     */
    @PostMapping("/me/photo")
    public ResponseEntity<UserProfileResponse> uploadPhoto(
            @AuthenticationPrincipal String firebaseUid,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(userService.uploadProfilePhoto(firebaseUid, file));
    }

    /**
     * DELETE /api/v1/users/me
     * Soft-deletes the authenticated user account (isDeleted = true).
     * The record remains in the DB.
     */
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteAccount(
            @AuthenticationPrincipal String firebaseUid) {
        userService.softDeleteUser(firebaseUid);
        return ResponseEntity.noContent().build();
    }
}
