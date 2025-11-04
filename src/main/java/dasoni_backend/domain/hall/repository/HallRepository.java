package dasoni_backend.domain.hall.repository;

import dasoni_backend.domain.hall.entity.Hall;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface HallRepository extends JpaRepository<Hall, Long> {

    // 관리하는 추모관을 최신순으로 조회
    List<Hall> findAllByAdminIdOrderByCreatedAtDesc(Long adminId);

    // 사용자가 입장(팔로우)한 추모관을 최신순으로 조회
    // 네이티브 쿼리는 추후 변경
    @Query(value = "SELECT h.* FROM halls h JOIN hall_followers hf ON h.id = hf.hall_id WHERE hf.user_id = :userId ORDER BY h.created_at DESC", nativeQuery = true)
    List<Hall> findAllByFollowerUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);
}
