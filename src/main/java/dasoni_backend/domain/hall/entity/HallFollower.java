package dasoni_backend.domain.hall.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "hall_followers")
public class HallFollower {

    @EmbeddedId
    private HallFollowerId hallFollowerId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}

