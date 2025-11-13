package dasoni_backend.domain.hall.entity;

import dasoni_backend.domain.user.entity.User;
import dasoni_backend.domain.voice.entity.Voice;
import dasoni_backend.global.enums.Personality;
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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
    private LocalDate birthday;

    @Column(name = "deadday")
    private LocalDate deadday;

    @Column(name = "profile", columnDefinition = "TEXT")
    private String profile;

    // ElementCollection 사용해 List로 변경
    // 3개로 고정
    @ElementCollection(targetClass = Personality.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "hall_target_natures", joinColumns = @JoinColumn(name = "hall_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "target_nature", length = 100)
    @Builder.Default
    private List<Personality> targetNatures = new ArrayList<>();

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "user_num")
    private Integer userNum;

    @Column(name = "is_opened")
    private Boolean isOpened;

    @Column(name = "docs", columnDefinition = "TEXT")
    private String docs;

    // 추모관에 띄울 한줄 소개 추가
    @Column(name = "review", columnDefinition = "TEXT")
    private String review;

    // 추모관 비공개 여부
    @Column(name = "is_secret")
    private boolean isSercret = true;
}

