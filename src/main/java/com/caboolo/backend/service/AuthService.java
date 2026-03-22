package com.caboolo.backend.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    public FirebaseToken verifyToken(String idToken) throws FirebaseAuthException {
        // This will verify the token's signature and expiration with Firebase
        return FirebaseAuth.getInstance().verifyIdToken(idToken);
    }
}
