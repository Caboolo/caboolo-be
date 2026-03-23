package com.caboolo.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response payload carrying the user's profile information.
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileResponse {

    private Long id;
    private String firebaseUid;
    private String phoneNumber;
    private String displayName;
    private String email;
    private String photoUrl;
    private String role;
    private LocalDateTime dateCreated;
    private LocalDateTime lastModified;
}
