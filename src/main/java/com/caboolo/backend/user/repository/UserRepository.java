package com.caboolo.backend.user.repository;

import com.caboolo.backend.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByFirebaseUid(String firebaseUid);

    Optional<User> findByPhoneNumber(String phoneNumber);

    /** Used for authenticated lookups — excludes soft-deleted users. */
    Optional<User> findByFirebaseUidAndIsDeletedFalse(String firebaseUid);
}
