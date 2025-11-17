package dasoni_backend.domain.letter.service;

import dasoni_backend.domain.hall.entity.Hall;
import dasoni_backend.domain.hall.repository.HallRepository;
import dasoni_backend.domain.letter.converter.LetterConverter;
import dasoni_backend.domain.letter.dto.LetterDTO.LetterPreCheckResponseDTO;
import dasoni_backend.domain.letter.dto.LetterDTO.LetterSaveRequestDTO;
import dasoni_backend.domain.letter.dto.LetterDTO.SentLetterCalenderListResponseDTO;
import dasoni_backend.domain.letter.dto.LetterDTO.SentLetterDetailResponseDTO;
import dasoni_backend.domain.letter.dto.LetterDTO.SentLetterListResponseDTO;
import dasoni_backend.domain.letter.dto.LetterDTO.TempLetterDetailResponseDTO;
import dasoni_backend.domain.letter.dto.LetterDTO.TempLetterListResponseDTO;
import dasoni_backend.domain.letter.dto.LetterDTO.myLetterRequestDTO;
import dasoni_backend.domain.letter.entity.Letter;
import dasoni_backend.domain.letter.repository.LetterRepository;
import dasoni_backend.domain.relationship.entity.Relationship;
import dasoni_backend.domain.relationship.repository.RelationshipRepository;
import dasoni_backend.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LetterServiceImpl implements LetterService{

    private final LetterRepository letterRepository;
    private final HallRepository hallRepository;
    private final RelationshipRepository relationshipRepository;

    // 1. 보낸 편지함 목록 조회
    @Transactional(readOnly = true)
    @Override
    public SentLetterListResponseDTO getSentLetterList(Long hallId, User user) {
        // 해당 추모관이 없거나 로그인하지 않았을 경우, 빈 리스트 반환(수정 불가능)
        if(hallId == null) {
            return LetterConverter.toSentLetterListResponseDTO(List.of());
        }

        List<Letter> letters = letterRepository.findAllByHall_IdAndUser_IdAndIsCompletedTrueOrderByCompletedAtDesc(hallId, user.getId());

        return LetterConverter.toSentLetterListResponseDTO(letters);
    }

    // 2. 보낸 편지함 달력 조회
    @Transactional(readOnly = true)
    @Override
    public SentLetterCalenderListResponseDTO getSentLetterCalenderList(Long hallId, User user, int year, int month) {
        // 로그인하지 않았을 경우, 빈 리스트 반환(수정 불가능)
        if(hallId == null) {
            return LetterConverter.toSentLetterCalenderListResponseDTO(List.of());
        }

        if (month < 1 || month > 12) throw new IllegalArgumentException("month는 1~12");

        var ym = java.time.YearMonth.of(year, month);
        var start = ym.atDay(1).atStartOfDay();
        var end = ym.plusMonths(1).atDay(1).atStartOfDay();

        List<Letter> days =  letterRepository.findAllByHall_IdAndUser_IdAndIsCompletedTrueAndCompletedAtGreaterThanEqualAndCompletedAtLessThanOrderByCompletedAtAsc(
                hallId, user.getId(), start, end);

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
    public LetterPreCheckResponseDTO getLetterPreCheck(Long hallId, User user) {

        Hall hall = hallRepository.findById(hallId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        // 음성이 되어있는지 관리자가 설정되어 있는지
        boolean isOpen = hall.getIsOpened();

        // 본인이 설정이 되어 있는 지
        Relationship relationship = relationshipRepository.findByHallAndUser(hall,user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        boolean isSet = relationship.getIsSet();

        return LetterPreCheckResponseDTO.builder()
                .isOpen(isOpen)
                .isSet(isSet)
                .build();
    }

    // 5. 추모관에 편지 쓰기 / 임시저장
    @Transactional
    @Override
    public void saveLetter(Long hallId, User user, LetterSaveRequestDTO request) {

        Hall hall = hallRepository.findById(hallId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        System.out.println("✅ request.isCompleted = " + request.isCompleted());

        // :TODO 일단 테스트를 위해서
//        // isCompleted=true면 오늘 이미 보냈는지 검사
//        if (request.isCompleted()
//                && letterRepository.existsByHall_IdAndUser_IdAndIsCompletedTrueAndCompletedAtBetween(
//                hallId, user.getId(), LocalDate.now().atStartOfDay(), LocalDate.now().plusDays(1).atStartOfDay()
//        )) {
//            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 편지를 보냈어요");
//        }
        Letter letter = LetterConverter.RequestToLetter(request, hall, user);
        letterRepository.save(letter);
    }

    // 임시보관함 조회
    @Transactional
    @Override
    public TempLetterListResponseDTO getTempLetterList(Long hallId, User user) {
        if(hallId == null)
            return LetterConverter.toTempLetterListRespnoseDTO(List.of());

        List<Letter> letters = letterRepository.findAllByHall_IdAndUser_IdAndIsCompletedFalseOrderByCreatedAtDesc(hallId, user.getId());

        return LetterConverter.toTempLetterListRespnoseDTO(letters);
    }

    // 임시보관 편지 내용 상세 확인
    @Transactional
    @Override
    public TempLetterDetailResponseDTO getTempLetterDetail(Long hallId, Long letterId) {
        Letter letter = letterRepository.findById(letterId)
                .orElseThrow(() -> new RuntimeException("편지를 찾을 수 없습니다."));
        return LetterConverter.totempLetterDetailResponseDTO(letter);
    }

    // 임시보과  편지 삭제
    @Transactional
    @Override
    public void deleteTempLetter(Long hallId, Long letterId, User user) {

        Letter letter = letterRepository.findById(letterId)
                        .orElseThrow(()-> new IllegalArgumentException("편지를 찾을 수 없습니다."));

        if(!letter.getHall().getId().equals(hallId)) {
            throw new IllegalArgumentException("해당 홀의 편지가 아닙니다.");
        }

        letterRepository.delete(letter);
    }

    @Override
    @Transactional
    public void deleteSendLetter(Long hallId, Long letterId, User user){
        // 홀 조회
        Hall hall = hallRepository.findById(hallId)
                .orElseThrow(() -> new IllegalArgumentException("추모관을 찾을 수 없습니다."));

        // 편지 조회
        Letter letter = letterRepository.findById(letterId)
                .orElseThrow(() -> new IllegalArgumentException("편지를 찾을 수 없습니다."));

        // 편지가 해당 홀에 속하는지 확인
        if (!letter.getHall().getId().equals(hallId)) {
            throw new IllegalArgumentException("해당 추모관의 편지가 아닙니다.");
        }

        // 작성자 확인
        if (!letter.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("본인이 작성한 편지만 삭제할 수 있습니다.");
        }

        // 완료된 편지인지 확인
        if (!letter.getIsCompleted()) {
            throw new IllegalArgumentException("완료되지 않은 편지는 삭제할 수 없습니다.");
        }

        // 편지 삭제
        letterRepository.delete(letter);
    }

    @Override
    @Transactional
    public void sendMeLetter(Long letterId, myLetterRequestDTO request, User user){

        Hall hall = hallRepository.findBySubjectId(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("본인 추모관이 존재하지 않습니다."));

        //:TODO 이미 오늘 편지를 보냈는지 확인

        Letter letter = LetterConverter.toMyLetterEntity(request, hall, user);
        letterRepository.save(letter);
    }
}
