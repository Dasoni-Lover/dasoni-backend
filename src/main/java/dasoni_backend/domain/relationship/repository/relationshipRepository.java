package dasoni_backend.domain.relationship.repository;

import dasoni_backend.domain.relationship.entity.Relationship;
import org.springframework.data.jpa.repository.JpaRepository;

public interface relationshipRepository extends JpaRepository<Relationship, Integer> {
}
