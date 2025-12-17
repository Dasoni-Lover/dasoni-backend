package dasoni_backend.domain.reply.service;

import dasoni_backend.domain.hall.entity.Hall;
import dasoni_backend.domain.hall.repository.HallRepository;
import dasoni_backend.domain.letter.converter.LetterConverter;
import dasoni_backend.domain.letter.dto.LetterDTO.ReceiveLetterListResponseDTO;
import dasoni_backend.domain.letter.entity.Letter;
import dasoni_backend.domain.letter.repository.LetterRepository;
import dasoni_backend.domain.notification.service.NotificationService;
import dasoni_backend.domain.relationship.entity.Relationship;
import dasoni_backend.domain.relationship.repository.RelationshipRepository;
import dasoni_backend.domain.reply.converter.ReplyConverter;
import dasoni_backend.domain.reply.dto.ReplyDTO.AiReplyCreateResponseDTO;
import dasoni_backend.domain.reply.dto.ReplyDTO.ReplyResponseDTO;
import dasoni_backend.domain.reply.entity.Reply;
import dasoni_backend.domain.reply.repository.ReplyRepository;
import dasoni_backend.domain.user.entity.User;
import dasoni_backend.global.S3.service.S3Service;
import dasoni_backend.global.ai.service.GeminiVoiceScriptServiceImpl;
import dasoni_backend.global.enums.NotificationKind;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ReplyServiceImpl implements ReplyService {

    private final HallRepository hallRepository;
    private final LetterRepository letterRepository;
    private final ReplyRepository replyRepository;
    private final GeminiVoiceScriptServiceImpl geminiVoiceScriptService;
    private final RelationshipRepository relationshipRepository;

    private final TextToSpeechModel tts;
    private final S3Service s3Service;
    private final NotificationService notificationService;

    @Override
    public AiReplyCreateResponseDTO TestcreateAiReply(Long hallId, Long letterId, User user) {

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

        // 6. 한 달 이내 완료된 편지 중 최신 2개 조회
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);

        List<Letter> recentTwo = letterRepository
                .findTop3ByHall_IdAndUser_IdAndIsCompletedTrueOrderByCompletedAtDesc(
                        hallId, user.getId()
                ).stream()
                .filter(l -> l.getCompletedAt() != null && l.getCompletedAt().isAfter(oneMonthAgo))
                .limit(2)
                .toList();

        // 7. 감정 편지
        String p1Emotion = "";
        String p2Emotion = "";
        if (!recentTwo.isEmpty()) {
            p1Emotion = recentTwo.getFirst().getContent();
            if (recentTwo.size() > 1) {
                p2Emotion = recentTwo.getLast().getContent();
            }
        }

        // 이번 편지(content)는 current로, 나머지 2개만 감정 방향성 참고용으로 넘김 -> 얘도 체크박스로 변경
        String currentLetterContent = targetLetter.getContent();

        // 고인 정보
        Relationship relationship = relationshipRepository
                .findByHallIdAndUserId(hallId, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "관계 정보를 찾을 수 없습니다."));

        String relationDetail =
                relationship.getDetail() != null ? relationship.getDetail() : "";

        String deceasedInsight =
                relationship.getExplanation() != null ? relationship.getExplanation() : "";

        String tone =
                Boolean.TRUE.equals(relationship.getPolite()) ? "존댓말" : "반말";

        String frequentWords =
                relationship.getSpeakHabit() != null ?relationship.getSpeakHabit() : "";

        String calledName =
                relationship.getCalledName() != null ? relationship.getCalledName() : "";

        List<String> userDescriptions = relationship.getNatures() != null
                ? relationship.getNatures().stream().map(p -> p.getValue()).toList()
                : List.of();

        // 고인 정보 끝

        // GeminiAI 답장 스크립트 생성
//        String script = geminiVoiceScriptService.generateVoiceReplyScript(
//                currentLetterContent,
//                recentOthers
//        );
        String script = geminiVoiceScriptService.generateVoiceReplyScript(
                currentLetterContent,
                p1Emotion + p2Emotion,
                relationDetail,
                deceasedInsight,
                tone,
                frequentWords,
                calledName,
                userDescriptions
        );
        // Script -> Reply Voice
        byte[] audioBytes = generateTtsAudio(script, voiceId);

        log.info("\n스크립트 완료!\n");
        // S3 업로드 key 생성
        String s3Key = "audios/replies/" + hallId + "/" + UUID.randomUUID() + ".mp3";

        // S3 음성 업로드
        s3Service.uploadFile(s3Key, audioBytes, "audio/mpeg");

        log.info("\n음성 업로드 완료!\n");

        // Reply 엔티티 생성
        Reply reply = Reply.builder()
                .hall(hall)
                .user(user)          // 누가 요청했는지(나중에 필요하면 고인 계정으로 바꿔도 됨)
                .letter(targetLetter)
                .content(script)
                .s3Key(s3Key)
                .checked(false)
                .createdAt(LocalDateTime.now())
                .build();

        log.info("\n엔티티 생성 완료!\n");

        replyRepository.save(reply);

        return AiReplyCreateResponseDTO.builder()
                .replyId(reply.getId())
                .content(reply.getContent())
                .audioUrl(reply.getS3Key())
                .isChecked(reply.isChecked())
                .createdAt(reply.getCreatedAt())
                .build();
    }

    @Override
    public ReceiveLetterListResponseDTO getReceiveLetterList(Long hallId, User user) {

        Hall hall = hallRepository.findById(hallId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "추모관을 찾을 수 없습니다."));

        List<Reply> replies = replyRepository.findAllByHall_IdAndUser_IdOrderByCreatedAtDesc(hallId, user.getId());

        return LetterConverter.toReceiveLetterListResponseDTO(replies);
    }

    public void createAiReply(Long hallId, Long letterId, User user){
        // 추모관 검증
        log.info("[AI] 1.createAiReply 진입: hallId={}, letterId={}, userId={}",
                hallId, letterId, user.getId());

        Hall hall = hallRepository.findById(hallId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "추모관을 찾을 수 없습니다."));

        log.info("[AI] 2.홀 검증");
        // 편지 검증
        Letter targetLetter = letterRepository.findById(letterId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "편지를 찾을 수 없습니다."));
        log.info("[AI] 3.편지 검증");

        // 해당 추모관이 맞는지 검증
        if (!targetLetter.getHall().getId().equals(hallId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "해당 추모관의 편지가 아닙니다.");
        }
        log.info("[AI] 4.추모관 검증");

        // 편지 작성자 검증 (본인이 쓴 편지에 대해서만 AI 답장 생성)
        if (!targetLetter.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인이 작성한 편지만 AI 답장을 생성할 수 있습니다.");
        }
        log.info("[AI] 5.작성자 검증");

        // 고인 음성 존재 여부 체크
        if(hall.getVoice() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "고인 음성이 설정되지 않았습니다. 먼저 음성 파일 업로드 및 voiceId 생성을 완료해주세요.");
        }
        String voiceId = hall.getVoice().getVoiceId();
        log.info("[AI] 6.음성 검증");

        // 한 달 이내 완료된 편지 중 최신 3개 조회 (방금 쓴 거 + 이전 2개)
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);

        List<Letter> recentThree =
            letterRepository.findTop3ByHall_IdAndUser_IdAndIsCompletedTrueAndCompletedAtAfterOrderByCompletedAtDesc(
                    hallId, user.getId(), oneMonthAgo
            );

        // currentLetter는 별도로 사용
        String currentLetterContent = targetLetter.getContent();

        // recentThree에서 currentLetter 제외하고 앞에서부터 2개 사용
        List<Letter> previousLetters = recentThree.stream()
            .filter(l -> !l.getId().equals(targetLetter.getId()))
            .limit(2)
            .toList();

        log.info("\n{}개 편지 조회 완료!\n",recentThree.size());

        // 나머지 두개는 감정평가 용
        String previousEmotions = previousLetters.stream()
            .map(Letter::getContent)
            .collect(Collectors.joining("\n\n"));

        // 고인 정보
        Relationship relationship = relationshipRepository
            .findByHallIdAndUserId(hallId, user.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "관계 정보를 찾을 수 없습니다."));

        List<String> userDescriptions =
            relationship.getNatures() != null
            ? relationship.getNatures()
                .stream()
                .map(p -> p.getValue())
                .toList()
           : List.of();

        String relationDetail =
                relationship.getDetail() != null ? relationship.getDetail() : "";

        String Explanation =
                relationship.getExplanation() != null ? relationship.getExplanation() : "";

        String tone =
                Boolean.TRUE.equals(relationship.getPolite()) ? "존댓말" : "반말";

        String frequentWords =
                relationship.getSpeakHabit() != null ?relationship.getSpeakHabit() : "";

        String calledName =
                relationship.getCalledName() != null ? relationship.getCalledName() : "";
        // 고인 정보 끝

        log.info("\n고인 정보 끝!\n");

        String script = geminiVoiceScriptService.generateVoiceReplyScript(
                currentLetterContent, // 현재 편지
                previousEmotions,  // 감정 평가 용
                relationDetail, // 자세한 관계
                Explanation, // 고인 설명
                tone,
                frequentWords,
                calledName,
                userDescriptions
        );
        log.info("\n스크립트 완료!\n내용:\n{}",script);

        // Script -> Reply Voice
        byte[] audioBytes = generateTtsAudio(script, voiceId);

        // S3 업로드 key 생성
        String s3Key = "audios/replies/" + hallId + "/" + UUID.randomUUID() + ".mp3";

        // S3 음성 업로드
        s3Service.uploadFile(s3Key, audioBytes, "audio/mpeg");

        log.info("\n음성 업로드 완료!\n");

        // Reply 엔티티 생성
        Reply reply = Reply.builder()
                .hall(hall)
                .user(user)          // 누가 요청했는지(나중에 필요하면 고인 계정으로 바꿔도 됨)
                .letter(targetLetter)
                .content(script)
                .s3Key(s3Key)
                .checked(false)
                .createdAt(LocalDateTime.now())
                .build();

        log.info("\n엔티티 생성 완료!\n");

        replyRepository.save(reply);
        notificationService.createNotification(hall, user, NotificationKind.REPLY_ARRIVED);
    }

    private byte[] generateTtsAudio(String text, String voiceId) {

        try{
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
            log.info("TTS 생성 완료: voiceId={}, textLength={}", voiceId, text.length());
            return response.getResult().getOutput();
        } catch (Exception e) {
            log.error("TTS generation failed. voiceId={}", voiceId, e);
            return null;
        }
    }

    @Override
    @Transactional
    public ReplyResponseDTO getOneReply(Long hallId, Long replyId, User user) {
        Hall hall = hallRepository.findById(hallId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "추모관을 찾을 수 없습니다."));
        Reply reply = replyRepository.findById(replyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "답장을 찾을 수 없습니다."));

        if(!reply.getHall().equals(hall)){ throw new RuntimeException("잘못된 답장입니다: 추모관과 답장 불일치"); }

        // 확인된걸로 바꿈
        reply.setChecked(true);
        ReplyResponseDTO dto = ReplyConverter.toReplyResponseDTO(reply);

        if (reply.getS3Key() != null) {
            String audioUrl = s3Service.generatePresignedDownloadUrl(reply.getS3Key());
            dto.setAudioUrl(audioUrl);
        }

        return dto;
    }
}
