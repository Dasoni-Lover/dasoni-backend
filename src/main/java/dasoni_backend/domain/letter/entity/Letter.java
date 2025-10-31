package dasoni_backend.domain.letter.entity;

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

import java.time.LocalDateTime;

@Entity
@Table(name = "letters")
public class Letter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hall_id", nullable = false)
    private Hall hall;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "to_name", length = 50, nullable = false)
    private String toName;

    @Column(name = "from_name", length = 50, nullable = false)
    private String fromName;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "is_completed", nullable = false)
    private Boolean isCompleted = false;
}
