package com.caboolo.backend.auth.provider;

public interface OtpProvider {
    void sendOtp(String phoneNumber);
    boolean verifyOtp(String phoneNumber, String otp);
}
