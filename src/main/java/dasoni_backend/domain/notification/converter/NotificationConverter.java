package dasoni_backend.domain.notification.converter;

import dasoni_backend.domain.notification.dto.NotificationDTO.NotificationListDTO;
import dasoni_backend.domain.notification.dto.NotificationDTO.NotificationResponseDTO;
import dasoni_backend.domain.notification.entity.Notification;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class NotificationConverter {
    // Notification → ResponseDTO 변환
    public static NotificationResponseDTO toDTO(Notification notification) {
        return NotificationResponseDTO.builder()
                .notificationId(notification.getId())
                .hallId(notification.getHall().getId())
                .kind(notification.getKind().getDescription())
                .title(notification.getTitle())  // "故 박영수 추모관"
                .body(notification.getKind().getBodyMessage())  // Enum에서 body 가져오기
                .date(notification.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))) // 생성 날짜
                .build();
    }

    // List<Notification> → ListDTO 변환
    public static NotificationListDTO toListDTO(List<Notification> notifications) {
        List<NotificationResponseDTO> notificationList = notifications.stream()
                .map(NotificationConverter::toDTO)
                .collect(Collectors.toList());

        return NotificationListDTO.builder()
                .notificationCount((long) notifications.size())
                .notifications(notificationList)
                .build();
    }
}
