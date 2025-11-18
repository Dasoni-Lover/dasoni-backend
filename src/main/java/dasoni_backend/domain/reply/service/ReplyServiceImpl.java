package dasoni_backend.domain.reply.service;

import dasoni_backend.domain.hall.entity.Hall;
import dasoni_backend.domain.hall.repository.HallRepository;
import dasoni_backend.domain.letter.entity.Letter;
import dasoni_backend.domain.letter.repository.LetterRepository;
import dasoni_backend.domain.reply.dto.ReplyDTO.AiReplyCreateResponseDTO;
import dasoni_backend.domain.reply.entity.Reply;
import dasoni_backend.domain.reply.repository.ReplyRepository;
import dasoni_backend.domain.user.entity.User;
import dasoni_backend.global.ai.service.GeminiVoiceScriptServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ReplyServiceImpl implements ReplyService {

    private final HallRepository hallRepository;
    private final LetterRepository letterRepository;
    private final ReplyRepository replyRepository;
    private final GeminiVoiceScriptServiceImpl geminiVoiceScriptService;

    @Override
    public AiReplyCreateResponseDTO createAiReply(Long hallId, Long letterId, User user) {
        // 추모관 검증
        Hall hall = hallRepository.findById(hallId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "추모관을 찾을 수 없습니다."));

        // 편지 검증
        Letter targetLetter = letterRepository.findById(letterId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "편지를 찾을 수 없습니다."));

        if (!targetLetter.getHall().getId().equals(hallId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "해당 추모관의 편지가 아닙니다.");
        }

        // 편지 작성자 검증 (본인이 쓴 편지에 대해서만 AI 답장 생성)
        if (!targetLetter.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인이 작성한 편지만 AI 답장을 생성할 수 있습니다.");
        }

        // 최근 편지들 조회(3개)
        List<Letter> recentLetters = letterRepository
                .findTop3ByHall_IdAndUser_IdAndIsCompletedTrueOrderByCompletedAtDesc(
                        hallId, user.getId()
                );

        // 이번 편지(content)는 current로, 나머지 2개만 감정 방향성 참고용으로 넘김
        String currentLetterContent = targetLetter.getContent();

        List<String> recentOthers = recentLetters.stream()
                .filter(l -> !l.getId().equals(letterId))   // 지금 편지는 빼고
                .limit(2)                                   // 1개만
                .map(Letter::getContent)
                .toList();

        // GeminiAI 답장 스크립트 생성
        String script = geminiVoiceScriptService.generateVoiceReplyScript(
                currentLetterContent,
                recentOthers
        );

        // Reply 엔티티 생성 (일단 audioUrl은 null, 나중에 ElevenLabs + S3 붙이기)
        Reply reply = Reply.builder()
                .hall(hall)
                .user(user)          // 누가 요청했는지(나중에 필요하면 고인 계정으로 바꿔도 됨)
                .letter(targetLetter)
                .content(script)
                .audioUrl(null)      // :TODO ElevenLabs TTS + S3 연동 후 URL 저장
                .isAi(true)
                .isChecked(false)
                .createdAt(LocalDateTime.now())
                .build();

        replyRepository.save(reply);

        return AiReplyCreateResponseDTO.builder()
                .replyId(reply.getId())
                .content(reply.getContent())
                .audioUrl(reply.getAudioUrl())
                .isAi(reply.getIsAi())
                .isChecked(reply.getIsChecked())
                .createdAt(reply.getCreatedAt())
                .build();
    }
}
