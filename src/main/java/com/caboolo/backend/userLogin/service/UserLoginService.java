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
        log.info("Handling login for uid={}, phone={}", uid, phoneNumber);

        UserLogin userLogin;

        // 1. Try to find by phoneNumber (our primary stable identifier now)
        Optional<UserLogin> userByPhone = (phoneNumber != null)
                ? userLoginRepository.findByPhoneNumber(phoneNumber)
                : Optional.empty();

        if (userByPhone.isPresent()) {
            userLogin = userByPhone.get();
            log.info("User found by phone number. Internal userId={}", userLogin.getUserId());
            // If firebaseUid changed (e.g. re-login after account deletion in firebase), update it
            if (!uid.equals(userLogin.getFirebaseUid())) {
                log.info("Updating firebaseUid from {} to {} for userId={}", userLogin.getFirebaseUid(), uid, userLogin.getUserId());
                userLogin.setFirebaseUid(uid);
                userLogin = userLoginRepository.save(userLogin);
            }
        } else {
            // 2. Fallback: Try to find by firebaseUid (for existing users or if phone is missing)
            Optional<UserLogin> userByUid = userLoginRepository.findByFirebaseUid(uid);
            if (userByUid.isPresent()) {
                userLogin = userByUid.get();
                log.info("User found by firebaseUid. Internal userId={}", userLogin.getUserId());
                // Update phone number if we have one now
                if (phoneNumber != null && !phoneNumber.equals(userLogin.getPhoneNumber())) {
                    log.info("Linking phone number {} to userId={}", phoneNumber, userLogin.getUserId());
                    userLogin.setPhoneNumber(phoneNumber);
                    userLogin = userLoginRepository.save(userLogin);
                }
            } else {
                // 3. Truly new user
                log.info("New user detected, generating new internal userId");
                userLogin = UserLogin.Builder.userLogin()
                        .withUserId(sequenceGenerator.nextId())
                        .withFirebaseUid(uid)
                        .withPhoneNumber(phoneNumber)
                        .build();

                userLogin = userLoginRepository.save(userLogin);
                log.info("New UserLogin record created. userId={}, firebaseUid={}", userLogin.getUserId(), uid);
            }
        }

        log.info("Login handled successfully for userId={}", userLogin.getUserId());
        return UserLoginConverter.toAuthResponse(userLogin);
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
        return UserLoginConverter.toUserLoginDto(userLogin);
    }

    /**
     * Internal lookup by Firebase UID (used by security filters).
     */
    public UserLogin findByFirebaseUid(String firebaseUid) {
        return userLoginRepository.findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new EntityNotFoundException("No user login record found for firebaseUid: " + firebaseUid));
    }
}
