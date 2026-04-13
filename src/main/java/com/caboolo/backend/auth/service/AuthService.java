package com.caboolo.backend.auth.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AuthService {

    public FirebaseToken verifyToken(String idToken) throws FirebaseAuthException {
        log.debug("Verifying ID token with Firebase");
        // This will verify the token's signature and expiration with Firebase
        return FirebaseAuth.getInstance().verifyIdToken(idToken, false);
    }
}
