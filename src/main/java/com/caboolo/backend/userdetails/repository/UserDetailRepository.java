package com.caboolo.backend.userdetails.repository;

import com.caboolo.backend.userdetails.domain.UserDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

@Repository
public interface UserDetailRepository extends JpaRepository<UserDetail, Long> {
    Optional<UserDetail> findByUserId(String userId);

    Collection<UserDetail> findAllByUserIdIn(Collection<String> userIds);
}
