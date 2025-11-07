package dasoni_backend.domain.relationship.repository;

import dasoni_backend.domain.relationship.entity.RelationshipNature;
import org.springframework.data.jpa.repository.JpaRepository;

public interface relationshipNatureRepository extends JpaRepository<RelationshipNature, Long> {
}
