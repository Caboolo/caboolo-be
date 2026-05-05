package com.caboolo.backend.security;

import com.caboolo.backend.auth.service.AuthService;
import com.caboolo.backend.userLogin.domain.UserLogin;
import com.caboolo.backend.userLogin.repository.UserLoginRepository;
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
    private final UserLoginRepository userLoginRepository;

    public FirebaseTokenFilter(AuthService authService, UserLoginRepository userLoginRepository) {
        this.authService = authService;
        this.userLoginRepository = userLoginRepository;
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
                String phoneNumber = (String) decodedToken.getClaims().get("phone_number");
                if (phoneNumber != null) {
                    phoneNumber = phoneNumber.replaceAll("\\D", "");

                    if (phoneNumber.length() >= 10) {
                        phoneNumber = phoneNumber.substring(phoneNumber.length() - 10);
                    }
                }
                log.debug("Resolved phone number from token: {}", phoneNumber);

                // Look up internal userId by phone number
                UserLogin userLogin = userLoginRepository.findByPhoneNumber(phoneNumber)
                        .orElseThrow(() -> new RuntimeException("User not found for phone: " + phoneNumber));
                String internalUserId = userLogin.getUserId();
                log.debug("Resolved internal userId: {} for phone: {}", internalUserId, phoneNumber);

                UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(internalUserId, idToken, new ArrayList<>());

                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (Exception e) {
                log.error("Authentication failed: {}", e.getMessage());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"success\":false,\"message\":\"user logged out or not found. please relogin\",\"data\":null}");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
