package dasoni_backend.domain.hall.repository;

import dasoni_backend.domain.hall.entity.Hall;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HallRepository extends JpaRepository<Hall, Long> { }
