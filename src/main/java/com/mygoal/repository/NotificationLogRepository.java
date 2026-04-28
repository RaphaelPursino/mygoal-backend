package com.mygoal.repository;

import com.mygoal.entity.NotificationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface NotificationLogRepository extends JpaRepository<NotificationLog, UUID> {

    @Query("""
        SELECT COUNT(n) > 0 FROM NotificationLog n
        WHERE n.user.id = :userId
        AND n.sentAt >= :since
    """)
    boolean existsRecentNotification(UUID userId, LocalDateTime since);
}