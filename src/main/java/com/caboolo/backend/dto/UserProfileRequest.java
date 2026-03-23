package com.caboolo.backend.dto;

/**
 * Request body for updating a user's profile details.
 */
public class UserProfileRequest {

    private String displayName;
    private String email;

    public UserProfileRequest() {
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
