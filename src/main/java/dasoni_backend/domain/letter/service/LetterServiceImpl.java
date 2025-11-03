package dasoni_backend.domain.letter.service;

import dasoni_backend.domain.letter.converter.LetterConverter;
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
    public SentLetterListResponseDTO getSentLetterList(Long userId) {
        // 로그인하지 않았을 경우, 빈 리스트 반환(수정 불가능)
        if(userId == null) {
            return LetterConverter.toSentLetterListResponseDTO(List.of());
        }

        List<Letter> letters = letterRepository.findAllByUser_IdAndIsCompletedTrueOrderByCompletedAtDesc(userId);

        return LetterConverter.toSentLetterListResponseDTO(letters);
    }
}
