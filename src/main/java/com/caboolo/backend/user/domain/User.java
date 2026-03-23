package com.caboolo.backend.user.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String firebaseUid;

    @Column(unique = true)
    private String phoneNumber;

    @Column(length = 100)
    private String displayName;

    @Column(unique = true)
    private String email;

    /** CDN URL of the profile photo (e.g. Cloudinary secure_url). */
    @Column(columnDefinition = "TEXT")
    private String photoUrl;

    /**
     * Provider-specific public ID used to delete the old photo when a new one is uploaded.
     * e.g. "caboolo/profile_photos/abc123" for Cloudinary.
     */
    @Column
    private String photoPublicId;

    @Column(nullable = false)
    private String role = "ROLE_USER";

    /** Soft-delete flag — records are never physically removed. */
    @Column(nullable = false)
    private boolean isDeleted = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dateCreated = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime lastModified = LocalDateTime.now();

    @PreUpdate
    public void onUpdate() {
        this.lastModified = LocalDateTime.now();
    }

    public User(String firebaseUid, String phoneNumber) {
        this.firebaseUid = firebaseUid;
        this.phoneNumber = phoneNumber;
    }
}
