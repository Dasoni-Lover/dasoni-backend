package dasoni_backend.domain.relationship.entity;

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
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "relationship_natures")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RelationshipNature {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "relationship_id", nullable = false)
    private Relationship relationship;

    @Enumerated(EnumType.STRING)
    @Column(name = "nature", length = 50, nullable = false)
    private Personality nature;
}
