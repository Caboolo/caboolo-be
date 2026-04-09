package com.caboolo.backend.userLogin.service;

import com.caboolo.backend.auth.dto.AuthResponse;
import com.caboolo.backend.userLogin.converter.UserLoginConverter;
import com.caboolo.backend.userLogin.domain.UserLogin;
import com.caboolo.backend.userLogin.repository.UserLoginRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class UserLoginService {

    private final UserLoginRepository userLoginRepository;
    private final com.caboolo.backend.core.idgen.SequenceGenerator sequenceGenerator;

    public UserLoginService(UserLoginRepository userLoginRepository,
                            com.caboolo.backend.core.idgen.SequenceGenerator sequenceGenerator) {
        this.userLoginRepository = userLoginRepository;
        this.sequenceGenerator = sequenceGenerator;
    }

    // -----------------------------------------------------------------------
    // Auth flow
    // -----------------------------------------------------------------------

    public AuthResponse handleLogin(String uid, String phoneNumber) {
        Optional<UserLogin> existingUserOpt = userLoginRepository.findByFirebaseUid(uid);
        UserLogin userLogin;

        if (existingUserOpt.isEmpty()) {
            userLogin = UserLogin.Builder.userLogin()
                    .withUserLoginId(sequenceGenerator.nextId())
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
