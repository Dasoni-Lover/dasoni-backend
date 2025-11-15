package dasoni_backend.domain.request.repository;

import dasoni_backend.domain.hall.entity.Hall;
import dasoni_backend.domain.request.entity.Request;
import dasoni_backend.domain.user.entity.User;
import dasoni_backend.global.enums.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RequestRepository extends JpaRepository<Request, Long> {

    // Hall과 User와 Status로 찾기
    Optional<Request> findByHallAndUserAndStatus(Hall hall, User user, RequestStatus status);

    List<Request> findByHallAndStatus(Hall hall, RequestStatus status);
}