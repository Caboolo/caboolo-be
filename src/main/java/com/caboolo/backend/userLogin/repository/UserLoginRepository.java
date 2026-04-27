package com.caboolo.backend.userLogin.repository;

import com.caboolo.backend.userLogin.domain.UserLogin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserLoginRepository extends JpaRepository<UserLogin, Long> {

    Optional<UserLogin> findByFirebaseUid(String firebaseUid);

    Optional<UserLogin> findByUserId(String userId);

    Optional<UserLogin> findByPhoneNumber(String phoneNumber);

    /** Used for authenticated lookups — excludes soft-deleted users. */
    Optional<UserLogin> findByFirebaseUidAndIsDeletedFalse(String firebaseUid);

    Optional<UserLogin> findByUserIdAndIsDeletedFalse(String userId);
}
