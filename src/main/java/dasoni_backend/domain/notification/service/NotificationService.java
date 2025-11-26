package dasoni_backend.domain.notification.service;

import dasoni_backend.domain.hall.entity.Hall;
import dasoni_backend.domain.notification.dto.NotificationDTO.NotificationListDTO;
import dasoni_backend.domain.user.entity.User;
import dasoni_backend.global.enums.NotificationKind;

public interface NotificationService {
    NotificationListDTO getNotifications(User user);
    void closeNotification(Long notificationId, User user);
    void createNotification(Hall hall, User user, NotificationKind kind);
}
