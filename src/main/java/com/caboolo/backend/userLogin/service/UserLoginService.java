package com.caboolo.backend.userLogin.service;

import com.caboolo.backend.auth.dto.AuthResponse;
import com.caboolo.backend.core.idgen.SequenceGenerator;
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
    private final SequenceGenerator sequenceGenerator;

    private final UserLoginConverter userLoginConverter;

    public UserLoginService(UserLoginRepository userLoginRepository,
                            SequenceGenerator sequenceGenerator, UserLoginConverter userLoginConverter) {
        this.userLoginRepository = userLoginRepository;
        this.sequenceGenerator = sequenceGenerator;
        this.userLoginConverter = userLoginConverter;
    }

    // -----------------------------------------------------------------------
    // Auth flow
    // -----------------------------------------------------------------------

    /**
     * Handles login for a given phone number.
     * <p>
     * If a record already exists for that phone number the existing internal
     * {@code userId} is returned unchanged.  If no record exists a new one
     * is created with a sequence-generated {@code userId}.
     *
     * @param phoneNumber the phone number extracted from the Firebase token
     * @return an {@link AuthResponse} containing the internal userId
     */
    public AuthResponse handleLogin(String phoneNumber) {
        log.info("Handling login for phone={}", phoneNumber);

        UserLogin userLogin;

        Optional<UserLogin> userByPhone = (phoneNumber != null)
                ? userLoginRepository.findByPhoneNumber(phoneNumber)
                : Optional.empty();

        if (userByPhone.isPresent()) {
            userLogin = userByPhone.get();
            log.info("Existing user found by phone number. Internal userId={}", userLogin.getUserId());
        } else {
            // New user — generate a stable internal userId
            log.info("New user detected, generating new internal userId");
            userLogin = UserLogin.Builder.userLogin()
                    .withUserLoginId(sequenceGenerator.nextId())
                    .withUserId(sequenceGenerator.nextId())
                    .withPhoneNumber(phoneNumber)
                    .build();

            userLogin = userLoginRepository.save(userLogin);
            log.info("New UserLogin record created. userId={}", userLogin.getUserId());
        }

        log.info("Login handled successfully for userId={}", userLogin.getUserId());
        return userLoginConverter.toAuthResponse(userLogin);
    }

    // -----------------------------------------------------------------------
    // Lookup
    // -----------------------------------------------------------------------

    /**
     * Finds a UserLogin record by its internal sequence-generated userId.
     *
     * @param userId the internal userId
     * @return the corresponding {@link UserLoginDto}
     * @throws EntityNotFoundException if no record exists for the given userId
     */
    public UserLoginDto findByUserId(String userId) {
        UserLogin userLogin = userLoginRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("No user login record found for userId: " + userId));
        return userLoginConverter.toUserLoginDto(userLogin);
    }
}
