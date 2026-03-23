package com.caboolo.backend.waitlist.repository;

import com.caboolo.backend.waitlist.domain.WaitlistEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WaitlistRepository extends JpaRepository<WaitlistEntry, Long> {
    Optional<WaitlistEntry> findByEmail(String email);
    boolean existsByEmail(String email);
}
