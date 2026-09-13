package com.bankjatim.jims.repository;

import com.bankjatim.jims.domain.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Notification> findByTargetRoleOrderByCreatedAtDesc(String role);
    long countByUserIdAndIsReadFalse(Long userId);
}
