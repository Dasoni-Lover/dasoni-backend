package dasoni_backend.domain.relationship.repository;

import dasoni_backend.domain.hall.entity.Hall;
import dasoni_backend.domain.relationship.entity.Relationship;
import dasoni_backend.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface relationshipRepository extends JpaRepository<Relationship, Integer> {
    boolean existsByHallAndUser(Hall hall, User user);
}
