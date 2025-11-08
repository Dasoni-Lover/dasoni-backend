package dasoni_backend.domain.photo.repository;

import dasoni_backend.domain.photo.entity.Photo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PhotoRepository extends JpaRepository<Photo, Long> {
    List<Photo> findByUserId(Long userId);
    List<Photo> findByHallId(Long hallId);
    Optional<Photo> findByIdAndHallId(Long photoId, Long hallId);
    // 내 사진만 (특정 추모관)
    @Query("""
      select p from Photo p
      where p.hall.id = :hallId
        and p.user.id = :userId
      order by p.uploadedAt desc, p.id desc
    """)
    List<Photo> findMyPhotos(@Param("hallId") Long hallId, @Param("userId") Long userId);

    // 전체 앨범 (권한 로직은 서비스에서 필터링)
    @Query("""
      select p from Photo p
      where p.hall.id = :hallId
      order by p.uploadedAt desc, p.id desc
    """)
    List<Photo> findAllByHall(@Param("hallId") Long hallId);
}

