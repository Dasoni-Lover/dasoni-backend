package dasoni_backend.domain.notification.repository;

import dasoni_backend.domain.notification.entity.Notification;
import dasoni_backend.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    // 안읽은 알림만 (최신순)
    List<Notification> findByUserAndIsReadFalseOrderByCreatedAtDesc(User user);
}
