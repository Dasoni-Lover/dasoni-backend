package dasoni_backend.domain.relationship.repository;

import dasoni_backend.domain.hall.entity.Hall;
import dasoni_backend.domain.relationship.entity.Relationship;
import dasoni_backend.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RelationshipRepository extends JpaRepository<Relationship, Long> {
    boolean existsByHallAndUser(Hall hall, User user);

    // Hall로 조회
    List<Relationship> findByHall(Hall hall);

    // Hall ID로 조회
    List<Relationship> findByHallId(Long hallId);

    // User로 조회
    List<Relationship> findByUser(User user);

    // User ID로 조회
    List<Relationship> findByUserId(Long userId);

    // Hall과 User로 조회
    Optional<Relationship> findByHallAndUser(Hall hall, User user);

    // Hall ID와 User ID로 조회
    Optional<Relationship> findByHallIdAndUserId(Long hallId, Long userId);

    // 본인만 빼고 조회
    List<Relationship> findByHallAndUserIdNot(Hall hall, Long userId);
}
