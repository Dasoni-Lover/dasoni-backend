package dasoni_backend.domain.voice.service;

import dasoni_backend.domain.hall.entity.Hall;
import dasoni_backend.domain.hall.repository.HallRepository;
import dasoni_backend.domain.user.entity.User;
import dasoni_backend.domain.voice.dto.VoiceDTOs.VoiceDTO;
import dasoni_backend.domain.voice.entity.Voice;
import dasoni_backend.domain.voice.repository.VoiceRepository;
import dasoni_backend.global.S3.service.FileUploadService;
import dasoni_backend.global.elevenlabs.ElevenLabsClient;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class VoiceServiceImpl implements VoiceService {

    private final FileUploadService fileUploadService;
    private final HallRepository hallRepository;
    private final ElevenLabsClient elevenLabsClient;
    private final VoiceRepository voiceRepository;

    @Override
    @Transactional
    public void uploadVoice(Long hallId, VoiceDTO request, User user){
        Hall hall = hallRepository.findById(hallId)
                .orElseThrow(() -> new EntityNotFoundException("Hall not found"));

        if (!hall.getAdmin().getId().equals(user.getId())) {
            throw new IllegalStateException("권한이 없습니다");
        }

        Voice voice = Voice.builder()
                .s3Key(request.getUrl())
                .updateAt(LocalDateTime.now())
                .build();

        voiceRepository.save(voice);
        hall.setVoice(voice);
        hallRepository.save(hall);
    }

    @Override
    @Transactional
    public void updateVoice(Long hallId, VoiceDTO request, User user) {

        Hall hall = hallRepository.findById(hallId)
                .orElseThrow(() -> new EntityNotFoundException("Hall not found"));

        if (!hall.getAdmin().getId().equals(user.getId())) {
            throw new IllegalStateException("권한이 없습니다");
        }

        // 기존 Voice 보관 (삭제는 나중에)
        Voice oldVoice = hall.getVoice();

        // 새 Voice 생성
        Voice newVoice = Voice.builder()
                .s3Key(request.getUrl())
                .updateAt(LocalDateTime.now())
                .build();

        voiceRepository.save(newVoice);

        //  Hall에 새 Voice 연결
        hall.setVoice(newVoice);
        hallRepository.save(hall);

        log.info("새 Voice 저장 완료 → voiceId: {}, url: {}",
                newVoice.getId(), newVoice.getS3Key());

        log.info("Hall FK 업데이트 완료 → hallId: {}, hall.voiceId: {}",
                hall.getId(), newVoice.getId());

        // 트랜잭션 커밋 후 S3 + DB 삭제
        if (oldVoice != null && oldVoice.getS3Key() != null) {
            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            try {
                                String oldS3Key = fileUploadService.extractS3Key(oldVoice.getS3Key());
                                fileUploadService.deleteFile(oldS3Key);
                                voiceRepository.delete(oldVoice);
                                log.info("기존 Voice 삭제 완료 (AFTER_COMMIT): {}", oldS3Key);
                            } catch (Exception e) {
                                log.warn("기존 Voice 삭제 실패 (AFTER_COMMIT): {}", e.getMessage());
                            }
                        }
                    }
            );
        }
    }

    @Override
    @Transactional
    public void deleteVoice(Long hallId, User user){
        Hall hall = hallRepository.findById(hallId)
                .orElseThrow(() -> new EntityNotFoundException("Hall not found"));
        if (!hall.getAdmin().getId().equals(user.getId())) {
            throw new IllegalStateException("권한이 없습니다");
        }
        Voice oldVoice = hall.getVoice();
        String oldUrl = oldVoice.getS3Key();
        fileUploadService.deleteFile(fileUploadService.extractS3Key(oldUrl));
        voiceRepository.delete(oldVoice);
        hall.setVoice(null);
    }

    @Override
    @Transactional(readOnly = true)
    public VoiceDTO getVoice(Long hallId, User user){
        Hall hall = hallRepository.findById(hallId)
                .orElseThrow(() -> new EntityNotFoundException("Hall not found"));

        if (!hall.getAdmin().getId().equals(user.getId())) {
            throw new IllegalStateException("권한이 없습니다");
        }

        Voice voice = hall.getVoice();
        log.info(">> hall.getVoice() 결과: {}", voice != null ? voice.getId() : null);

        if (voice == null) {
            return VoiceDTO.builder()
                    .url(null)
                    .build();
        }
        return VoiceDTO.builder()
                .url(voice.getS3Key())
                .build();
    }

    @Override
    @Transactional
    public void generateVoiceId(Long hallId, User user) {
        Hall hall = hallRepository.findById(hallId)
                .orElseThrow(() -> new EntityNotFoundException("Hall not found"));

        if (!hall.getAdmin().getId().equals(user.getId())) {
            throw new IllegalStateException("권한이 없습니다");
        }

        Voice voice = hall.getVoice();
        if(voice == null || voice.getS3Key() == null) {
            throw new IllegalStateException("먼저 음성 파일을 업로드해야 합니다.");
        }

        String key = fileUploadService.extractS3Key(voice.getS3Key());
        byte[] audioBytes = fileUploadService.downloadFile(key);

        String name = hall.getName() + "_voice";
        String voiceId = elevenLabsClient.createIVCVoice(audioBytes, name);

        voice.setVoiceId(voiceId);
        voice.setUpdateAt(LocalDateTime.now());
    }
}
