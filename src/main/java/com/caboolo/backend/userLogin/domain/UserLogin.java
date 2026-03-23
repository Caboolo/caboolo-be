package com.caboolo.backend.userLogin.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.caboolo.backend.core.domain.GenericIdEntity;

@Entity
@Table(name = "user_login")
@Data
@NoArgsConstructor
public class UserLogin extends GenericIdEntity {

    @Column(nullable = false, unique = true)
    private String firebaseUid;

    @Column(unique = true)
    private String phoneNumber;

    public UserLogin(String firebaseUid, String phoneNumber) {
        this.firebaseUid = firebaseUid;
        this.phoneNumber = phoneNumber;
    }
}
