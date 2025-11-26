package dasoni_backend.domain.notification.entity;

import dasoni_backend.domain.hall.entity.Hall;
import dasoni_backend.domain.user.entity.User;
import dasoni_backend.global.enums.NotificationKind;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "notifications")
public class Notification {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hall_id", nullable = false)
    private Hall hall;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "kind", nullable = false)
    private NotificationKind kind;

    @Column(name = "title", length = 200, nullable = false)
    private String title;

    @Column(name = "body", columnDefinition = "TEXT", nullable = false)
    private String body;

    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 읽음 처리
    public void markAsRead() {
        this.isRead = true;
    }

    // 생성 메소드
    public static Notification create(Hall hall, User user, NotificationKind kind) {
        return Notification.builder()
                .hall(hall)
                .user(user)
                .kind(kind)
                .title(generateTitle(hall))  // 제목 생성
                .body(kind.getBodyMessage())  // Enum에서 본문 가져오기
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private static String generateTitle(Hall hall) {
        return String.format("故 %s 추모관", hall.getDeceasedName());
    }


}


