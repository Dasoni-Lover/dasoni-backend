package dasoni_backend.domain.letter.service;

import dasoni_backend.domain.letter.converter.LetterConverter;
import dasoni_backend.domain.letter.dto.LetterDTO.SentLetterCalenderListResponseDTO;
import dasoni_backend.domain.letter.dto.LetterDTO.SentLetterCalenderListResponseDTO.SentLetterCalenderResponseDTO;
import dasoni_backend.domain.letter.dto.LetterDTO.SentLetterDetailResponseDTO;
import dasoni_backend.domain.letter.dto.LetterDTO.SentLetterListResponseDTO;
import dasoni_backend.domain.letter.dto.LetterDTO.SentLetterListResponseDTO.SentLetterResponseDTO;
import dasoni_backend.domain.letter.entity.Letter;
import dasoni_backend.domain.letter.repository.LetterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LetterServiceImpl implements LetterService{

    private final LetterRepository letterRepository;

    // 1. 보낸 편지함 목록 조회
    @Transactional
    @Override
    public SentLetterListResponseDTO getSentLetterList(Long hallId, Long userId) {
        // 해당 추모관이 없거나 로그인하지 않았을 경우, 빈 리스트 반환(수정 불가능)
        if(hallId == null || userId == null) {
            return LetterConverter.toSentLetterListResponseDTO(List.of());
        }

        List<Letter> letters = letterRepository.findAllByHall_IdAndUser_IdAndIsCompletedTrueOrderByCompletedAtDesc(hallId, userId);

        return LetterConverter.toSentLetterListResponseDTO(letters);
    }

    // 2. 보낸 편지함 달력 조회
    @Transactional
    @Override
    public SentLetterCalenderListResponseDTO getSentLetterCalenderList(Long hallId, Long userId, int year, int month) {
        // 로그인하지 않았을 경우, 빈 리스트 반환(수정 불가능)
        if(hallId == null || userId == null) {
            return LetterConverter.toSentLetterCalenderListResponseDTO(List.of());
        }

        if (month < 1 || month > 12) throw new IllegalArgumentException("month는 1~12");

        var ym = java.time.YearMonth.of(year, month);
        var start = ym.atDay(1).atStartOfDay();
        var end = ym.plusMonths(1).atDay(1).atStartOfDay();

        List<Letter> days =  letterRepository.findAllByHall_IdAndUser_IdAndIsCompletedTrueAndCompletedAtGreaterThanEqualAndCompletedAtLessThanOrderByCompletedAtAsc(
                hallId, userId, start, end);

        return LetterConverter.toSentLetterCalenderListResponseDTO(days);
    }

    // 3. 보낸 편지 내용 상세 조회
    @Transactional
    @Override
    public SentLetterDetailResponseDTO getSentLetterDetail(Long hallId, Long letterId) {

        Letter letter = letterRepository.findById(letterId)
                .orElseThrow(() -> new RuntimeException("편지를 찾을 수 없습니다."));

        return LetterConverter.toSentLetterDetailResponseDTO(letter);
    }
}
