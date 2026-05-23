package com.caboolo.backend.notification.repository;

import com.caboolo.backend.notification.domain.UserFcmToken;
import com.caboolo.backend.notification.enums.FcmTokenStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserFcmTokenRepository extends JpaRepository<UserFcmToken, Long> {

    List<UserFcmToken> findByUserId(String userId);

    List<UserFcmToken> findAllByUserIdAndStatus(String userId, FcmTokenStatus status);
    
    List<UserFcmToken> findAllByUserIdInAndStatus(Collection<String> userIds, FcmTokenStatus status);

    Optional<UserFcmToken> findByFcmToken(String fcmToken);

    Optional<UserFcmToken> findByUserIdAndDeviceId(String userId, String deviceId);

    @Modifying
    @Query("UPDATE UserFcmToken t SET t.status = :status WHERE t.fcmToken IN :tokens")
    void updateStatusByTokens(List<String> tokens, FcmTokenStatus status);

    @Modifying
    @Query("DELETE FROM UserFcmToken t WHERE t.status = 'EXPIRED' AND t.lastModified < :cutoffDate")
    void deleteExpiredTokensOlderThan(LocalDateTime cutoffDate);
}
