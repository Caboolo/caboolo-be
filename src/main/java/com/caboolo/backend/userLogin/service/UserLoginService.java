package com.caboolo.backend.userLogin.service;

import com.caboolo.backend.auth.dto.AuthResponse;
import com.caboolo.backend.userLogin.converter.UserLoginConverter;
import com.caboolo.backend.userLogin.domain.UserLogin;
import com.caboolo.backend.userLogin.dto.UserLoginDto;
import com.caboolo.backend.userLogin.repository.UserLoginRepository;
import jakarta.persistence.EntityNotFoundException;
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
        log.info("Handling login for uid={}", uid);
        Optional<UserLogin> existingUserOpt = userLoginRepository.findByFirebaseUid(uid);
        UserLogin userLogin;

        if (existingUserOpt.isEmpty()) {
            log.info("New user detected, creating UserLogin record for uid={}", uid);
            userLogin = UserLogin.Builder.userLogin()
                    .withUserLoginId(sequenceGenerator.nextId())
                    .withFirebaseUid(uid)
                    .withPhoneNumber(phoneNumber)
                    .build();

            userLogin = userLoginRepository.save(userLogin);
            log.info("New UserLogin record created for uid={}", uid);
        } else {
            userLogin = existingUserOpt.get();
            // Update phone number if missing or changed
            if (phoneNumber != null && !phoneNumber.equals(userLogin.getPhoneNumber())) {
                log.info("Updating phone number for uid={}", uid);
                userLogin.setPhoneNumber(phoneNumber);
                userLogin = userLoginRepository.save(userLogin);
            }
        }
        log.info("Login handled successfully for uid={}", uid);
        return UserLoginConverter.toAuthResponse(userLogin);
    }

    // -----------------------------------------------------------------------
    // Lookup
    // -----------------------------------------------------------------------

    /**
     * Finds a UserLogin record by its Firebase UID (used as the external userId).
     *
     * @param userId the Firebase UID of the user
     * @return the corresponding {@link UserLoginDto}
     * @throws EntityNotFoundException if no record exists for the given userId
     */
    public UserLoginDto findByUserId(String userId) {
        UserLogin userLogin = userLoginRepository.findByFirebaseUid(userId)
                .orElseThrow(() -> new EntityNotFoundException("No user login record found for userId: " + userId));
        return UserLoginConverter.toUserLoginDto(userLogin);
    }
}
