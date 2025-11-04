package dasoni_backend.domain.user.repository;

import dasoni_backend.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByLogId(String logId);
}
