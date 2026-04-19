package com.caboolo.backend.userdetails.controller;

import com.caboolo.backend.core.controller.BaseController;
import com.caboolo.backend.core.dto.RestEntity;
import com.caboolo.backend.dto.UserDetailRequestDto;
import com.caboolo.backend.review.dto.ProfileDto;
import com.caboolo.backend.review.dto.ReviewDto;
import com.caboolo.backend.review.service.ReviewService;
import com.caboolo.backend.userdetails.dto.UserDetailResponseDto;
import com.caboolo.backend.userdetails.service.UserDetailService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/userdetails")
public class UserDetailsController extends BaseController {

    private final UserDetailService userDetailService;
    private final ReviewService reviewService;

    public UserDetailsController(UserDetailService userDetailService, ReviewService reviewService) {
        this.userDetailService = userDetailService;
        this.reviewService = reviewService;
    }

    @PostMapping
    public RestEntity<UserDetailResponseDto> saveUserDetails(@Valid @RequestBody UserDetailRequestDto requestDto) {
        try {
            UserDetailResponseDto responseDto = userDetailService.createOrUpdateUserDetails(requestDto);
            return successResponse(responseDto, "User details saved successfully");
        } catch (IllegalArgumentException e) {
            return errorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return errorResponse("Failed to save user details: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



    /**
     * GET /api/userdetails/profile
     * Returns the authenticated user's full profile.
     */
    @GetMapping("/profile")
    public RestEntity<UserDetailResponseDto> getProfile(
            @AuthenticationPrincipal String firebaseUid) {
        return successResponse(userDetailService.getProfile(firebaseUid), "Profile retrieved successfully");
    }

    /**
     * PUT /api/userdetails/profile
     * Updates display name and/or email.
     * Body: { "displayName": "...", "email": "..." }
     */
    @PutMapping("/profile")
    public RestEntity<UserDetailResponseDto> updateProfile(
            @AuthenticationPrincipal String firebaseUid,
            @Valid @RequestBody UserDetailRequestDto request) {
        return successResponse(userDetailService.updateProfile(firebaseUid, request), "Profile updated successfully");
    }

    /**
     * POST /api/userdetails/profile/photo
     * Uploads (or replaces) the authenticated user's profile photo.
     * Content-Type: multipart/form-data  —  field name: "file"
     */
    @PostMapping("/profile/photo")
    public RestEntity<String> uploadPhoto(
            @AuthenticationPrincipal String firebaseUid,
            @RequestParam("file") MultipartFile file) {
        return successResponse(userDetailService.uploadProfilePhoto(firebaseUid, file), "Photo uploaded successfully");
    }

    /**
     * DELETE /api/userdetails/profile
     * Soft-deletes the authenticated user account (isDeleted = true).
     * The record remains in the DB.
     */
    @DeleteMapping("/profile")
    public RestEntity<Void> deleteAccount(
            @AuthenticationPrincipal String firebaseUid) {
        userDetailService.softDeleteUser(firebaseUid);
        return successResponse("Account deleted successfully");
    }

    /**
     * GET /api/userdetails/{id}/photo
     * Resolves the user's profile photo.
     */
    @GetMapping("/{id}/photo")
    public RestEntity<String> getUserPhoto(@PathVariable String id) {
        String photoUrl = userDetailService.getPhotoUrlByUserId(id);
        if (photoUrl == null || photoUrl.isBlank()) {
            return errorResponse("Not Found", HttpStatus.NOT_FOUND);
        }
        return successResponse(photoUrl, "Photo URL retrieved successfully");
    }

    @GetMapping("/review/summary")
    public RestEntity<ProfileDto> getMyProfileHeader(@RequestParam String userId) {
        return successResponse(userDetailService.getMyProfileHeader(userId));
    }

    @GetMapping("/review/listing")
    public RestEntity<List<ReviewDto>> getReviewDtoList(@RequestParam String userId) {
        return successResponse(reviewService.getReviewDtoList(userId));
    }
}
