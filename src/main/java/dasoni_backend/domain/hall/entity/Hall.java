package dasoni_backend.domain.hall.entity;

import dasoni_backend.domain.user.entity.User;
import dasoni_backend.domain.voice.entity.Voice;
import dasoni_backend.global.enums.Personality;
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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

import static lombok.AccessLevel.PROTECTED;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = PROTECTED)
@Table(name = "halls")
public class Hall {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FK -> users.id (관리자)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", nullable = false)
    private User admin;

    // FK -> voices.id
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voice_id")
    private Voice voice;

    // 추모 대상 관련 필드들
    @Column(name = "subject_id")
    private Long subjectId;

    @Column(name = "place")
    private String place;

    @Column(name = "phone")
    private String phone;

    @Column(name = "name")
    private String name;

    @Column(name = "birthday")
    private LocalDateTime birthday;

    @Column(name = "deadday")
    private LocalDateTime deadday;

    @Column(name = "profile", columnDefinition = "TEXT")
    private String profile;

    // target_nature: 여러 값 가능 시 JSON/Text 확장 고려
    @Enumerated(EnumType.STRING)
    @Column(name = "target_nature", length = 32)
    private Personality targetNature;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "user_num")
    private Integer userNum;

    @Column(name = "is_opened")
    private Boolean isOpened;

    @Column(name = "docs", columnDefinition = "TEXT")
    private String docs;
}

