package dasoni_backend.domain.reply.service;

import dasoni_backend.domain.hall.entity.Hall;
import dasoni_backend.domain.hall.repository.HallRepository;
import dasoni_backend.domain.letter.converter.LetterConverter;
import dasoni_backend.domain.letter.dto.LetterDTO.ReceiveLetterListResponseDTO;
import dasoni_backend.domain.letter.entity.Letter;
import dasoni_backend.domain.letter.repository.LetterRepository;
import dasoni_backend.domain.reply.dto.ReplyDTO.AiReplyCreateResponseDTO;
import dasoni_backend.domain.reply.entity.Reply;
import dasoni_backend.domain.reply.repository.ReplyRepository;
import dasoni_backend.domain.user.entity.User;
import dasoni_backend.global.S3.service.S3Service;
import dasoni_backend.global.ai.service.GeminiVoiceScriptServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.audio.tts.TextToSpeechModel;
import org.springframework.ai.audio.tts.TextToSpeechPrompt;
import org.springframework.ai.audio.tts.TextToSpeechResponse;
import org.springframework.ai.elevenlabs.ElevenLabsTextToSpeechOptions;
import org.springframework.ai.elevenlabs.api.ElevenLabsApi;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ReplyServiceImpl implements ReplyService {

    private final HallRepository hallRepository;
    private final LetterRepository letterRepository;
    private final ReplyRepository replyRepository;
    private final GeminiVoiceScriptServiceImpl geminiVoiceScriptService;

    private final TextToSpeechModel tts;
    private final S3Service s3Service;

    @Override
    public AiReplyCreateResponseDTO createAiReply(Long hallId, Long letterId, User user) {
        // 추모관 검증
        Hall hall = hallRepository.findById(hallId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "추모관을 찾을 수 없습니다."));

        // 편지 검증
        Letter targetLetter = letterRepository.findById(letterId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "편지를 찾을 수 없습니다."));

        // 해당 추모관이 맞는지 검증
        if (!targetLetter.getHall().getId().equals(hallId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "해당 추모관의 편지가 아닙니다.");
        }

        // 편지 작성자 검증 (본인이 쓴 편지에 대해서만 AI 답장 생성)
        if (!targetLetter.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인이 작성한 편지만 AI 답장을 생성할 수 있습니다.");
        }

        // 고인 음성 존재 여부 체크
        if(hall.getVoice() == null || hall.getVoice().getVoiceId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "고인 음성이 설정되지 않았습니다. 먼저 음성 파일 업로드 및 voiceId 생성을 완료해주세요.");
        }
        String voiceId = hall.getVoice().getVoiceId();

        // 최근 편지들 조회(3개) -> 추후 체크박스로 변경해야함
        List<Letter> recentLetters = letterRepository
                .findTop3ByHall_IdAndUser_IdAndIsCompletedTrueOrderByCompletedAtDesc(
                        hallId, user.getId()
                );

        // 이번 편지(content)는 current로, 나머지 2개만 감정 방향성 참고용으로 넘김 -> 얘도 체크박스로 변경
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

        // Script -> Reply Voice
        byte[] audioBytes = generateTtsAudio(script, voiceId);

        // S3 업로드 key 생성
        String s3Key = "audios/replies/" + hallId + "/" + UUID.randomUUID() + ".mp3";

        // S3 음성 업로드
        s3Service.uploadFile(s3Key, audioBytes, "audio/mpeg");

        // 음성 파일의 URL 생성
        String audioUrl = s3Service.getS3Url(s3Key);


        // Reply 엔티티 생성 (일단 audioUrl은 null, 나중에 ElevenLabs + S3 붙이기)
        Reply reply = Reply.builder()
                .hall(hall)
                .user(user)          // 누가 요청했는지(나중에 필요하면 고인 계정으로 바꿔도 됨)
                .letter(targetLetter)
                .content(script)
                .audioUrl(audioUrl)
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

    @Override
    public ReceiveLetterListResponseDTO getReceiveLetterList(Long hallId, User user) {

        Hall hall = hallRepository.findById(hallId)
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "추모관을 찾을 수 없습니다."));

        List<Reply> replies = replyRepository.findAllByHall_IdAndUser_IdOrderByCreatedAtDesc(hallId, user.getId());

        return LetterConverter.toReceiveLetterListResponseDTO(replies);
    }

    public byte[] generateTtsAudio(String text, String voiceId) {

        // stability, similarityBoost, style, useSpeakerBoost, speed
        var voiceSettings = new ElevenLabsApi.SpeechRequest.VoiceSettings((Double)0.4, (Double)0.75, (Double)0.0, Boolean.TRUE, (Double)0.85);

        var options = ElevenLabsTextToSpeechOptions.builder()
                .model("eleven_turbo_v2_5")
                .voiceId(voiceId)
                .voiceSettings(voiceSettings)
                .outputFormat("mp3_44100_128")
                .build();

        TextToSpeechPrompt prompt = new TextToSpeechPrompt(text, options);
        TextToSpeechResponse response = tts.call(prompt);

        return response.getResult().getOutput();
    }
}
