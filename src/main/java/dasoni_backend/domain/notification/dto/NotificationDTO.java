package dasoni_backend.domain.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class NotificationDTO {

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotificationResponseDTO{
        private Long notificationId;
        private Long hallId;
        private String kind;
        private String title;
        private String body;
        private String date;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotificationListDTO{
        private Long notificationCount;
        private List<NotificationResponseDTO> notifications;
    }
}
