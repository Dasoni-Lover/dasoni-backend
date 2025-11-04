package dasoni_backend.domain.letter.service;

import dasoni_backend.domain.hall.entity.Hall;
import dasoni_backend.domain.hall.repository.HallRepository;
import dasoni_backend.domain.letter.converter.LetterConverter;
import dasoni_backend.domain.letter.dto.LetterDTO.LetterPreCheckResponseDTO;
import dasoni_backend.domain.letter.dto.LetterDTO.LetterSaveRequestDTO;
import dasoni_backend.domain.letter.dto.LetterDTO.SentLetterCalenderListResponseDTO;
import dasoni_backend.domain.letter.dto.LetterDTO.SentLetterCalenderResponseDTO;
import dasoni_backend.domain.letter.dto.LetterDTO.SentLetterDetailResponseDTO;
import dasoni_backend.domain.letter.dto.LetterDTO.SentLetterListResponseDTO;
import dasoni_backend.domain.letter.dto.LetterDTO.SentLetterResponseDTO;
import dasoni_backend.domain.letter.entity.Letter;
import dasoni_backend.domain.letter.repository.LetterRepository;
import dasoni_backend.domain.user.entity.User;
import dasoni_backend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LetterServiceImpl implements LetterService{

    private final LetterRepository letterRepository;
    private final HallRepository hallRepository;
    private final UserRepository userRepository;

    // 1. 보낸 편지함 목록 조회
    @Transactional(readOnly = true)
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
    @Transactional(readOnly = true)
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
    @Transactional(readOnly = true)
    @Override
    public SentLetterDetailResponseDTO getSentLetterDetail(Long hallId, Long letterId) {

        Letter letter = letterRepository.findById(letterId)
                .orElseThrow(() -> new RuntimeException("편지를 찾을 수 없습니다."));

        return LetterConverter.toSentLetterDetailResponseDTO(letter);
    }

    // 4. 편지 보내기 버튼 눌렀을 경우
    @Transactional(readOnly = true)
    @Override
    public LetterPreCheckResponseDTO getLetterPreCheck(Long hallId, Long userId) {

        Hall hall = hallRepository.findById(hallId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));


        // 베타데모데이 전까지는 음성녹음 기능 구현 x -> 항상 오픈 상태로 두기
        boolean isOpen = true;

        // 일단 false로 해놓고 수요일날 수정
        boolean isSet = hasAiInfoTemp(hall);

        return LetterPreCheckResponseDTO.builder()
                .isOpen(isOpen)
                .isSet(isSet)
                .build();
    }
    // 추후 교체(letters.setting api 구현 후)
    private boolean hasAiInfoTemp(Hall hall) {
        boolean hasName = StringUtils.hasText(hall.getName());
        boolean hasAnyDate = hall.getBirthday() != null || hall.getDeadday() != null;
        return hasName && hasAnyDate;
    }

    // 5. 추모관에 편지 쓰기 / 임시저장
    @Override
    public void saveLetter(Long hallId, Long userId, LetterSaveRequestDTO request) {

        Hall hall = hallRepository.findById(hallId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        // isCompleted=true면 오늘 이미 보냈는지 검사
        if (request.isCompleted()
                && letterRepository.existsByHall_IdAndUser_IdAndIsCompletedTrueAndCompletedAtBetween(
                hallId, userId, LocalDate.now().atStartOfDay(), LocalDate.now().plusDays(1).atStartOfDay()
        )) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 편지를 보냈어요");
        }

        LocalDateTime now = LocalDateTime.now();

        Letter letter = new Letter();
        letter.setHall(hall);
        letter.setUser(user);
        letter.setToName(request.getToName());
        letter.setFromName(request.getFromName());
        letter.setContent(request.getContent());
        letter.setIsCompleted(request.isCompleted());
        letter.setCreatedAt(now);
        letter.setCompletedAt(request.isCompleted() ? now : null);

        letterRepository.save(letter);
    }
}
