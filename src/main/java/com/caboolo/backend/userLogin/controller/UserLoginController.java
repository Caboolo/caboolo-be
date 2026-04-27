package com.caboolo.backend.userLogin.controller;

import com.caboolo.backend.core.controller.BaseController;
import com.caboolo.backend.core.dto.RestEntity;
import com.caboolo.backend.userLogin.dto.UserLoginDto;
import com.caboolo.backend.userLogin.service.UserLoginService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/user-login")
public class UserLoginController extends BaseController {

    private final UserLoginService userLoginService;

    public UserLoginController(UserLoginService userLoginService) {
        this.userLoginService = userLoginService;
    }

    /**
     * GET /api/v1/user-login?userId={userId}
     *
     * Retrieves a UserLogin record by the internal generated userId.
     */
    @GetMapping
    public RestEntity<UserLoginDto> getUserLogin(@RequestParam String userId) {
        log.info("Received request to fetch UserLogin for userId={}", userId);
        UserLoginDto dto = userLoginService.findByUserId(userId);
        log.info("Successfully fetched UserLogin for userId={}", userId);
        return successResponse(dto, "User login record retrieved successfully");
    }
}
