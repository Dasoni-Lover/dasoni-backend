package dasoni_backend.domain.notification.service;

import dasoni_backend.domain.notification.dto.NotificationDTO.NotificationListDTO;
import dasoni_backend.domain.user.entity.User;

public interface NotificationService {
    NotificationListDTO getNotifications(User user);
    void closeNotification(Long notificationId, User user);
}
