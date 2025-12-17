package dasoni_backend.domain.letter.repository;

import dasoni_backend.domain.letter.entity.Letter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface LetterRepository extends JpaRepository<Letter, Long> {

    // 보낸 편지함 목록(추모관별, 사용자별, 최신순)
    List<Letter> findAllByHall_IdAndUser_IdAndIsCompletedTrueOrderByCompletedAtDesc(Long hallId, Long userId);

    // 보낸 편지함 달력 목록(추모관별, 사용자별, 최신순)
    List<Letter> findAllByHall_IdAndUser_IdAndIsCompletedTrueAndCompletedAtGreaterThanEqualAndCompletedAtLessThanOrderByCompletedAtAsc(
            Long hallId, Long userId, LocalDateTime start, LocalDateTime end);

    // 해당 추모관에서 오늘 이미 보낸 편지 있는지(isCompleted = true && completedAt 날짜 비교)
    boolean existsByHall_IdAndUser_IdAndIsCompletedTrueAndCompletedAtBetween(
            Long hallId, Long userId, LocalDateTime start, LocalDateTime end
    );

    // 임시보관함 조회
    List<Letter> findAllByHall_IdAndUser_IdAndIsCompletedFalseOrderByCreatedAtDesc(Long hallId, Long userId);

    // 최근 메시지 3개(완료된 편지만)
    List<Letter> findTop3ByHall_IdAndUser_IdAndIsCompletedTrueOrderByCompletedAtDesc(
            Long hallId, Long userId);

    List<Letter> findTop3ByHall_IdAndUser_IdAndIsCompletedTrueAndCompletedAtAfterOrderByCompletedAtDesc(
            Long hallId,
            Long userId,
            LocalDateTime completedAt
    );
}
