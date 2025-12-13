package dasoni_backend.domain.relationship.entity;

import dasoni_backend.domain.hall.entity.Hall;
import dasoni_backend.domain.user.entity.User;
import dasoni_backend.global.enums.Personality;
import dasoni_backend.global.enums.RelationKind;
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
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "relationships")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Relationship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hall_id", nullable = false)
    private Hall hall;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 친구, 연인, 가족 등
    @Enumerated(EnumType.STRING)
    @Column(name = "relation")
    private RelationKind relation;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "relationship_natures",
            joinColumns = @JoinColumn(name = "relationship_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "nature")
    private List<Personality> natures;

    // 자세한 관계 설명 - 방문자가 요청시 작성
    @Column(name = "detail", columnDefinition = "TEXT")
    private String detail;

    // 한줄평 (관리자, 방문자 모두)
    @Column(name = "review")
    private String review;

    // 고인에 대한 설명
    @Column(name = "explanation")
    private String explanation;

    // 말버릇
    @Column(name = "speak_habit")
    private String speakHabit;

    // 애칭
    @Column(name = "called_name")
    private String calledName;

    // 존댓말 여부
    @Column(name = "is_polite")
    private Boolean polite;

    // 오늘 편지를 보냈는지 여부
    @Column(name = "is_send")
    private boolean send;

    // AI 음성편지 설정을 마쳤는지 여부
    @Builder.Default
    @Column(name = "is_set")
    private boolean set = false;
}