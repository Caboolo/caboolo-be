package com.caboolo.backend.notification.repository;

import com.caboolo.backend.notification.domain.UserFcmToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserFcmTokenRepository extends JpaRepository<UserFcmToken, Long> {

    List<UserFcmToken> findByUserId(String userId);

    List<UserFcmToken> findByUserIdIn(Collection<String> userIds);

    Optional<UserFcmToken> findByUserIdAndFcmToken(String userId, String fcmToken);

    void deleteByUserIdAndFcmToken(String userId, String fcmToken);

    void deleteByFcmToken(String fcmToken);
}
