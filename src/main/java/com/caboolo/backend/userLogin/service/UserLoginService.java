package com.caboolo.backend.userLogin.service;

import com.caboolo.backend.auth.dto.AuthResponse;
import com.caboolo.backend.userLogin.converter.UserLoginConverter;
import com.caboolo.backend.userLogin.domain.UserLogin;
import com.caboolo.backend.userLogin.repository.UserLoginRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserLoginService {

    private final UserLoginRepository userLoginRepository;

    public UserLoginService(UserLoginRepository userLoginRepository) {
        this.userLoginRepository = userLoginRepository;
    }

    // -----------------------------------------------------------------------
    // Auth flow
    // -----------------------------------------------------------------------

    public AuthResponse handleLogin(String uid, String phoneNumber) {
        Optional<UserLogin> existingUserOpt = userLoginRepository.findByFirebaseUid(uid);
        UserLogin userLogin;

        if (existingUserOpt.isEmpty()) {
            userLogin = UserLogin.Builder.userLogin()
                    .withFirebaseUid(uid)
                    .withPhoneNumber(phoneNumber)
                    .build();

            userLogin = userLoginRepository.save(userLogin);
        } else {
            userLogin = existingUserOpt.get();
            // Update phone number if missing or changed
            if (phoneNumber != null && !phoneNumber.equals(userLogin.getPhoneNumber())) {
                userLogin.setPhoneNumber(phoneNumber);
                userLogin = userLoginRepository.save(userLogin);
            }
        }
        return UserLoginConverter.toAuthResponse(userLogin);
    }
}
