package com.caboolo.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request body for updating a user's profile details.
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileRequest {

    private String displayName;
    private String email;
}
