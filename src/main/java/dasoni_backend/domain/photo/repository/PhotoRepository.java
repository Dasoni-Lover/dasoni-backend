package dasoni_backend.domain.photo.repository;

import dasoni_backend.domain.photo.entity.Photo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PhotoRepository extends JpaRepository<Photo, Long> {
    List<Photo> findByUserId(Long userId);
    List<Photo> findByHallId(Long hallId);
    Optional<Photo> findByIdAndHallId(Long photoId, Long hallId);
}

