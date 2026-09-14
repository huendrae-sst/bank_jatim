package com.bankjatim.jims.service;

import com.bankjatim.jims.domain.Notification;
import com.bankjatim.jims.dto.NotificationResponse;
import com.bankjatim.jims.repository.NotificationRepository;
import com.bankjatim.jims.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotifications(UserPrincipal principal) {
        return notificationsFor(principal).stream()
                .map(NotificationResponse::from)
                .toList();
    }

    @Transactional
    public void markAllRead(Long userId) {
        notificationRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .filter(notification -> !Boolean.TRUE.equals(notification.getIsRead()))
                .forEach(notification -> {
                    notification.setIsRead(true);
                    notification.setReadAt(LocalDateTime.now());
                    notificationRepository.save(notification);
                });
    }

    private List<Notification> notificationsFor(UserPrincipal principal) {
        List<Notification> userNotifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(principal.getId());
        List<Notification> roleNotifications = notificationRepository.findByTargetRoleOrderByCreatedAtDesc(principal.getRole());

        return Stream.concat(userNotifications.stream(), roleNotifications.stream())
                .collect(Collectors.toMap(Notification::getId, notification -> notification, (left, right) -> left))
                .values()
                .stream()
                .sorted(Comparator.comparing(Notification::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }
}
