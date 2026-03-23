package com.caboolo.backend.userdetails.controller;

import com.caboolo.backend.core.controller.BaseController;
import com.caboolo.backend.core.dto.RestEntity;
import com.caboolo.backend.dto.UserProfileRequest;
import com.caboolo.backend.dto.UserProfileResponse;
import com.caboolo.backend.userLogin.service.UserLoginService;
import com.caboolo.backend.userdetails.domain.UserDetails;
import com.caboolo.backend.userdetails.dto.UserDetailRequestDto;
import com.caboolo.backend.userdetails.dto.UserDetailResponseDto;
import com.caboolo.backend.userdetails.service.UserDetailService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/userdetails")
public class UserDetailsController extends BaseController {

    private final UserDetailService userDetailService;
    private final UserLoginService userLoginService;

    public UserDetailsController(UserDetailService userDetailService, UserLoginService userLoginService) {
        this.userDetailService = userDetailService;
        this.userLoginService = userLoginService;
    }

    @PostMapping
    public RestEntity<UserDetailResponseDto> saveUserDetails(@RequestBody UserDetailRequestDto requestDto) {
        try {
            UserDetails savedDetails = userDetailService.saveOrUpdateUserDetails(requestDto);
            
            UserDetailResponseDto responseDto = new UserDetailResponseDto(
                    savedDetails.getId(),
                    savedDetails.getName(),
                    savedDetails.getUserId(),
                    savedDetails.getGender(),
                    savedDetails.getImageUrl()
            );

            return successResponse(responseDto, "User details saved successfully");
        } catch (IllegalArgumentException e) {
            return errorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return errorResponse("Failed to save user details: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



    /**
     * GET /api/v1/users/profile
     * Returns the authenticated user's full profile.
     */
    @GetMapping("/profile")
    public RestEntity<UserProfileResponse> getProfile(
            @AuthenticationPrincipal String firebaseUid) {
        return successResponse(userLoginService.getProfile(firebaseUid), "Profile retrieved successfully");
    }

    /**
     * PUT /api/v1/users/profile
     * Updates display name and/or email.
     * Body: { "displayName": "...", "email": "..." }
     */
    @PutMapping("/profile")
    public RestEntity<UserProfileResponse> updateProfile(
            @AuthenticationPrincipal String firebaseUid,
            @RequestBody UserProfileRequest request) {
        return successResponse(userLoginService.updateProfile(firebaseUid, request), "Profile updated successfully");
    }

    /**
     * POST /api/v1/users/profile/photo
     * Uploads (or replaces) the authenticated user's profile photo.
     * Content-Type: multipart/form-data  —  field name: "file"
     */
    @PostMapping("/profile/photo")
    public RestEntity<UserProfileResponse> uploadPhoto(
            @AuthenticationPrincipal String firebaseUid,
            @RequestParam("file") MultipartFile file) {
        return successResponse(userLoginService.uploadProfilePhoto(firebaseUid, file), "Photo uploaded successfully");
    }

    /**
     * DELETE /api/v1/users/profile
     * Soft-deletes the authenticated user account (isDeleted = true).
     * The record remains in the DB.
     */
    @DeleteMapping("/profile")
    public RestEntity<Void> deleteAccount(
            @AuthenticationPrincipal String firebaseUid) {
        userLoginService.softDeleteUser(firebaseUid);
        return successResponse("Account deleted successfully");
    }

    /**
     * GET /api/v1/users/{id}/photo
     * Resolves the user's profile photo.
     */
    @GetMapping("/{id}/photo")
    public RestEntity<String> getUserPhoto(@PathVariable Long id) {
        String photoUrl = userLoginService.getPhotoUrlByUserId(id);
        if (photoUrl == null || photoUrl.isBlank()) {
            return errorResponse("Not Found", HttpStatus.NOT_FOUND);
        }
        return successResponse(photoUrl, "Photo URL retrieved successfully");
    }
}
