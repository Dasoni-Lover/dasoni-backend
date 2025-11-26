package dasoni_backend.domain.notification.service;

import dasoni_backend.domain.notification.converter.NotificationConverter;
import dasoni_backend.domain.notification.dto.NotificationDTO.NotificationListDTO;
import dasoni_backend.domain.notification.entity.Notification;
import dasoni_backend.domain.notification.repository.NotificationRepository;
import dasoni_backend.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    @Override
    @Transactional
    public NotificationListDTO getNotifications(User user){
        List<Notification> notifications =
                notificationRepository.findByUserAndIsReadFalseOrderByCreatedAtDesc(user);
        return NotificationConverter.toListDTO(notifications);
    }

    @Override
    @Transactional
    public void closeNotification(Long notificationId, User user){
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NoSuchElementException("Notification with id " + notificationId + " does not exist"));
        notification.setIsRead(true);
    }
}
