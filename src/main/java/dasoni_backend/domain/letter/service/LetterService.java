package dasoni_backend.domain.letter.service;

import dasoni_backend.domain.letter.dto.LetterDTO.LetterCheckDTO;
import dasoni_backend.domain.letter.dto.LetterDTO.LetterPreCheckResponseDTO;
import dasoni_backend.domain.letter.dto.LetterDTO.LetterSaveRequestDTO;
import dasoni_backend.domain.letter.dto.LetterDTO.SentLetterCalenderListResponseDTO;
import dasoni_backend.domain.letter.dto.LetterDTO.SentLetterDetailResponseDTO;
import dasoni_backend.domain.letter.dto.LetterDTO.SentLetterListResponseDTO;
import dasoni_backend.domain.letter.dto.LetterDTO.TempLetterDetailResponseDTO;
import dasoni_backend.domain.letter.dto.LetterDTO.TempLetterListResponseDTO;
import dasoni_backend.domain.letter.dto.LetterDTO.myLetterRequestDTO;
import dasoni_backend.domain.relationship.dto.relationshipDTO.SettingDTO;
import dasoni_backend.domain.user.entity.User;

public interface LetterService {

    // 보낸 편지함 목록 조회
    SentLetterListResponseDTO getSentLetterList(Long hallId, User user);

    // 보낸 편지함 달력 조회
    SentLetterCalenderListResponseDTO getSentLetterCalenderList(Long hallId, User user, int year, int month);

    // 보낸 편지 내용 상세 조회
    SentLetterDetailResponseDTO getSentLetterDetail(Long hallId, Long letterId);

    // 편지 보내기 버튼 눌렀을 경우(고인 정보 입력 유무에 따라 다른 창 띄워줌)
    LetterPreCheckResponseDTO getLetterPreCheck(Long hallId, User user);

    // 추모관에 편지 쓰기 / 임시저장
    void saveLetter(Long hallId, User user, LetterSaveRequestDTO request);

    // 임시보관함 조회
    TempLetterListResponseDTO getTempLetterList(Long hallId, User user);

    // 임시보관 편지 내용 상세 조회
    TempLetterDetailResponseDTO getTempLetterDetail(Long hallId, Long letterId, User user);

    // 임시보관 편지 삭제
    void deleteTempLetter(Long hallId, Long letterId, User user);

    // 보낸 편지 삭제
    void deleteSendLetter(Long hallId,Long letterId, User user);

    // 본인추모관 편지 쓰기
    void sendMeLetter(myLetterRequestDTO request, User user);

    // AI 음성편지 설정 조회
    SettingDTO getLetterSettings(Long hallId, User user);

    // AI 음성편지 설정
    void createLetterSettings(Long hallId, SettingDTO request, User user);

    // AI 음성편지 설정 수정
    void updateLetterSettings(Long hallId, SettingDTO request, User user);

    // 오늘 편지 보냈는 지 여부
    LetterCheckDTO checkSentToday(Long hallId, User user);
}
