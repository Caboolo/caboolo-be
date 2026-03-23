package com.caboolo.backend.userdetails.controller;

import com.caboolo.backend.core.controller.BaseController;
import com.caboolo.backend.core.dto.RestEntity;
import com.caboolo.backend.userdetails.domain.UserDetails;
import com.caboolo.backend.userdetails.dto.UserDetailRequestDto;
import com.caboolo.backend.userdetails.dto.UserDetailResponseDto;
import com.caboolo.backend.userdetails.service.UserDetailService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/userdetails")
public class UserDetailsController extends BaseController {

    private final UserDetailService userDetailService;

    public UserDetailsController(UserDetailService userDetailService) {
        this.userDetailService = userDetailService;
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
}
