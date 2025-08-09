package com.talentcloud.notification_ms.repository;

import com.talentcloud.notification_ms.model.Notification;
import com.talentcloud.notification_ms.model.NotificationState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserIdOrderByCreatedAtDesc(String userId);
    List<Notification> findByEmailOrderByCreatedAtDesc(String email);



    long countByUserIdAndState(String userId, NotificationState state);


    long countByEmailAndState(String email, NotificationState state);

    // Combined query - get notifications by userId OR email
    @Query("SELECT n FROM Notification n WHERE " +
            "(n.userId = :userId AND :userId IS NOT NULL) OR " +
            "(n.email = :email AND :email IS NOT NULL) " +
            "ORDER BY n.createdAt DESC")
    List<Notification> findByUserIdOrEmailOrderByCreatedAtDesc(
            @Param("userId") String userId,
            @Param("email") String email);

    // Count unread notifications by userId OR email
    @Query("SELECT COUNT(n) FROM Notification n WHERE " +
            "n.state = :state AND (" +
            "(n.userId = :userId AND :userId IS NOT NULL) OR " +
            "(n.email = :email AND :email IS NOT NULL))")
    long countByUserIdOrEmailAndState(
            @Param("userId") String userId,
            @Param("email") String email,
            @Param("state") NotificationState state);

    // Find notifications where userId is null (for migration/cleanup purposes)
    List<Notification> findByUserIdIsNull();

    // Find notifications where email is null (for migration/cleanup purposes)
    List<Notification> findByEmailIsNull();

    long countByEmail(String email);

    long countByUserId(String userId);
}
