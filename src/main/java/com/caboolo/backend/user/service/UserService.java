package com.caboolo.backend.user.service;

import com.caboolo.backend.user.domain.User;
import com.caboolo.backend.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User handleLogin(String uid, String phoneNumber) {
        Optional<User> existingUserOpt = userRepository.findByFirebaseUid(uid);
        User user;

        if (existingUserOpt.isEmpty()) {
            user = new User(uid, phoneNumber);
            user = userRepository.save(user);
        } else {
            user = existingUserOpt.get();
            // Update phone number if missing or changed
            if (phoneNumber != null && !phoneNumber.equals(user.getPhoneNumber())) {
                user.setPhoneNumber(phoneNumber);
                user = userRepository.save(user);
            }
        }
        return user;
    }
}
