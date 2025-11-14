package dasoni_backend.domain.hall.repository;

import dasoni_backend.domain.hall.entity.Hall;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface HallRepository extends JpaRepository<Hall, Long> {

    @Query("""
            select h 
            from Hall h 
            where h.admin.id = :adminId 
            and (h.subjectId is null or h.subjectId <> :adminId)
            order by h.createdAt desc
            """)
    List<Hall> findAllManagedHallsExceptSelf(@Param("adminId") Long adminId);

    // 사용자가 입장(팔로우)한 추모관을 최신순으로 조회
    // 네이티브 쿼리는 추후 변경
    @Query(value = "SELECT h.* FROM halls h JOIN hall_followers hf ON h.id = hf.hall_id WHERE hf.user_id = :userId ORDER BY h.created_at DESC", nativeQuery = true)
    List<Hall> findAllByFollowerUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);

    // Optional로 둔 이유는 본인 추모관의 생성 여부를 확인하기 위함
    // existsByAdminId 같은 함수 따로 만들지 않아도 됨
    // 이 경우는 단순히 Id로만 조회하는거라 user 객체를 넘길 필요 없음
    Optional<Hall> findByAdminId(Long userId);

    // 본인 추모관인지 확인하기 위한
    Optional<Hall> findBySubjectId(Long subjectId);

    // 추모관 검색
    @Query("SELECT h FROM Hall h WHERE " +
            "(:name IS NULL OR h.name LIKE %:name%) AND " +
            "(:birthday IS NULL OR h.birthday = :birthday) AND " +
            "(:deadDay IS NULL OR h.deadday = :deadDay) AND " +
            "h.isSecret = false")
    List<Hall> searchHalls(
            @Param("name") String name,
            @Param("birthday") LocalDate birthday,
            @Param("deadDay") LocalDate deadDay
    );
}
