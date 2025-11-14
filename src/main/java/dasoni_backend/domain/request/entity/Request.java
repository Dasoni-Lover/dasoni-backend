package dasoni_backend.domain.request.entity;

import dasoni_backend.domain.hall.entity.Hall;
import dasoni_backend.domain.user.entity.User;
import dasoni_backend.global.enums.Personality;
import dasoni_backend.global.enums.RelationKind;
import dasoni_backend.global.enums.RequestStatus;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
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
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "requests")
public class Request {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hall_id", nullable = false)
    private Hall hall;

    @Enumerated(EnumType.STRING)
    @Column(name = "relation")
    private RelationKind relation;

    @Column(name = "detail", columnDefinition = "TEXT")
    private String detail;

    // 3개의 성격을 List로 저장
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "request_personalities",
            joinColumns = @JoinColumn(name = "request_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "personality")
    private List<Personality> natures;

    @Column(name = "review", columnDefinition = "TEXT")
    private String review;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private RequestStatus status;
}
