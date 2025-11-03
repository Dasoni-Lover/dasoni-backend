package dasoni_backend.domain.letter.repository;

import dasoni_backend.domain.letter.entity.Letter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LetterRepository extends JpaRepository<Letter, Long> {

    List<Letter> findAllByUser_IdAndIsCompletedTrueOrderByCompletedAtDesc(Long userId);
}
