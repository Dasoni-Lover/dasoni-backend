package dasoni_backend.domain.photo.entity;

import dasoni_backend.domain.hall.entity.Hall;
import dasoni_backend.domain.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.cglib.core.Local;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "photos")
public class Photo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hall_id", nullable = false)
    private Hall hall;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "url", columnDefinition = "TEXT")
    private String url;

    @Column(name = "is_private", nullable = false)
    private Boolean isPrivate = false;

    @Column(name = "is_ai", nullable = false)
    private Boolean isAi = false;

    @Column(name = "uploaded_at")
    private LocalDateTime uploadedAt;

    @Column(name = "occurred_at")
    private LocalDate occurredAt;

    public void updateContent(String content) {
        this.content = content;
    }

    public void updateOccurredAt(LocalDate occurredAt) {
        this.occurredAt = occurredAt;
    }

    public void updateIsPrivate(Boolean isPrivate) {
        this.isPrivate = isPrivate;
    }
}
