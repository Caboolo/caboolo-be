package com.caboolo.backend.security;

import com.caboolo.backend.auth.service.AuthService;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

@Slf4j
@Component
public class FirebaseTokenFilter extends OncePerRequestFilter {

    private final AuthService authService;

    public FirebaseTokenFilter(AuthService authService) {
        this.authService = authService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String idToken = authHeader.substring(7);
            log.debug("Authenticating request with Firebase token");
            try {
                FirebaseToken decodedToken = authService.verifyToken(idToken);
                String uid = decodedToken.getUid();
                log.debug("Found valid token for user: {}", uid);
                
                UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(uid, idToken, new ArrayList<>());
                
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (FirebaseAuthException e) {
                log.error("Firebase token verification failed: {}", e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }
}
