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
}
