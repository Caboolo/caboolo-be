package com.caboolo.backend.userdetails.repository;

import com.caboolo.backend.userdetails.domain.UserDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserDetailRepository extends JpaRepository<UserDetail, Long> {
    Optional<UserDetail> findByUserId(String userId);

    List<UserDetail> findByUserIdIn(Collection<String> userIds);

    @Query(value = "SELECT name from user_detail where is_deleted=false AND user_id = ?1", nativeQuery = true)
    String findNameByUserId(String userId);

    boolean existsByPhoneNumber(String phoneNumber);
}
