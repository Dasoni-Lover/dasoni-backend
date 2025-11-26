package dasoni_backend.domain.notification.controller;

import dasoni_backend.domain.notification.dto.NotificationDTO.NotificationListDTO;
import dasoni_backend.domain.notification.service.NotificationService;
import dasoni_backend.domain.user.entity.User;
import dasoni_backend.global.annotation.AuthUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/list")
    public ResponseEntity<NotificationListDTO> getNotifications(@AuthUser User user){
        return ResponseEntity.ok(notificationService.getNotifications(user));
    }
    @PatchMapping("/{notification_id}/close")
    public ResponseEntity<Void> closeNotification(@PathVariable("notification_id") Long notificationId,
                                                  @AuthUser User user){
        notificationService.closeNotification(notificationId,user);
        return ResponseEntity.ok().build();
    }
}
