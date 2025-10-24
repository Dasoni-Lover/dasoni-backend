package dasoni_backend.domain.relationship.entity;

import dasoni_backend.domain.hall.entity.Hall;
import dasoni_backend.domain.user.entity.User;
import dasoni_backend.global.enums.RelationKind;
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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
    @Column(name = "relation", length = 20)
    private RelationKind relation;

    // 영상 편지 사진
    @Column(name = "photo", columnDefinition = "TEXT")
    private String photo;

    // 말버릇
    @Column(name = "speak_habit")
    private String speakHabit;

    // 애칭
    @Column(name = "called_name")
    private String calledName;

    // 존댓말 여부
    @Column(name = "is_polite")
    private Boolean isPolite;

    // 오늘 편지를 보냈는지 여부
    @Column(name = "is_send")
    private Boolean isSend;

    // AI 영상편지 설정읆 마쳤는지 여부
    @Column(name = "is_set")
    private Boolean isSet;
}