package dasoni_backend.domain.letter.service;

import dasoni_backend.domain.letter.dto.LetterDTO.SentLetterDetailResponseDTO;
import dasoni_backend.domain.letter.dto.LetterDTO.SentLetterListResponseDTO;

public interface LetterService {

    // 보낸 편지함 목록 조회
    SentLetterListResponseDTO getSentLetterList(Long userId);

    // 보낸 편지함 달력 조회


    // 보낸 편지 내용 상세 조회
    SentLetterDetailResponseDTO getSentLetterDetail(Long hallId, Long letterId);

}
