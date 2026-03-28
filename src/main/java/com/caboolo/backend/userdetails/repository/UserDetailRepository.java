package com.caboolo.backend.userdetails.repository;

import com.caboolo.backend.userdetails.domain.UserDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserDetailRepository extends JpaRepository<UserDetails, Long> {
    Optional<UserDetails> findByUserId(String userId);
}
