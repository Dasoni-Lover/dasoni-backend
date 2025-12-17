package dasoni_backend.domain.voice.service;

import dasoni_backend.domain.hall.entity.Hall;
import dasoni_backend.domain.hall.repository.HallRepository;
import dasoni_backend.domain.user.entity.User;
import dasoni_backend.domain.voice.dto.VoiceDTOs.VoiceDTO;
import dasoni_backend.domain.voice.entity.Voice;
import dasoni_backend.domain.voice.repository.VoiceRepository;
import dasoni_backend.global.S3.service.FileUploadService;
import dasoni_backend.global.S3.service.S3Service;
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
    private final S3Service s3Service;
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

        String s3Key = fileUploadService.extractS3Key(request.getUrl());

        // 1. Voice 엔티티 먼저 생성 (voiceId는 null)
        Voice voice = Voice.builder()
                .s3Key(s3Key)
                .updateAt(LocalDateTime.now())
                .voiceId(null)
                .build();

        voiceRepository.save(voice);
        hall.setVoice(voice);

        log.info("등록: Voice 엔티티 저장 완료 → voiceId: {}, hallId: {}", voice.getId(), hall.getId());

        // 2. 즉시 voiceId 생성 (동기 처리)
        try {
            byte[] audioBytes = s3Service.downloadFile(s3Key);
            String newVoiceId = elevenLabsClient.createIVCVoice(audioBytes, hall.getName() + "_voice");

            voice.setVoiceId(newVoiceId);
            voice.setUpdateAt(LocalDateTime.now());
            // voiceRepository.save(voice); // @Transactional 내부라 자동 저장됨

            log.info("등록: ElevenLabs voiceId 생성 완료 → voiceId: {}", newVoiceId);
        } catch (Exception e) {
            log.error("voiceId 생성 실패 (upload) - hallId: {}, voiceId: {}", hallId, voice.getId(), e);
            // Voice 엔티티는 이미 저장되었으므로, 실패 시 정리 필요
            throw new RuntimeException("음성 등록에 실패했습니다. 잠시 후 다시 시도해주세요.", e);
        }
    }

    @Override
    @Transactional
    public void updateVoice(Long hallId, VoiceDTO request, User user) {
        Hall hall = hallRepository.findById(hallId)
                .orElseThrow(() -> new EntityNotFoundException("Hall not found"));

        if (!hall.getAdmin().getId().equals(user.getId())) {
            throw new IllegalStateException("권한이 없습니다");
        }

        // 기존 Voice 보관
        Voice oldVoice = hall.getVoice();

        // 새 Voice 생성
        String s3Key = fileUploadService.extractS3Key(request.getUrl());

        Voice newVoice = Voice.builder()
                .s3Key(s3Key)
                .voiceId(null)
                .updateAt(LocalDateTime.now())
                .build();

        voiceRepository.save(newVoice);
        hall.setVoice(newVoice);

        log.info("수정: 새 Voice 저장 완료 → voiceId: {}", newVoice.getId());
        log.info("수정: Hall FK 업데이트 완료 → hallId: {}", hall.getId());

        // 즉시 새 voiceId 생성 (동기 처리)
        try {
            byte[] audioBytes = s3Service.downloadFile(s3Key);
            String newVoiceId = elevenLabsClient.createIVCVoice(audioBytes, hall.getName() + "_voice");

            newVoice.setVoiceId(newVoiceId);
            newVoice.setUpdateAt(LocalDateTime.now());

            log.info("수정: ElevenLabs voiceId 생성 완료 → voiceId: {}", newVoiceId);
        } catch (Exception e) {
            log.error("voiceId 생성 실패 (update) - hallId: {}, voiceId: {}", hallId, newVoice.getId(), e);
            throw new RuntimeException("음성 수정에 실패했습니다. 잠시 후 다시 시도해주세요.", e);
        }

        // 트랜잭션 커밋 후 기존 Voice 정리
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        if (oldVoice != null) {
                            try {
                                if (oldVoice.getS3Key() != null) {
                                    s3Service.deleteFile(oldVoice.getS3Key());
                                    log.info("기존 Voice S3 파일 삭제 완료: {}", oldVoice.getS3Key());
                                }
                                if (oldVoice.getVoiceId() != null) {
                                    elevenLabsClient.deleteVoice(oldVoice.getVoiceId());
                                    log.info("기존 Voice ElevenLabs 삭제 완료: {}", oldVoice.getVoiceId());
                                }
                                voiceRepository.delete(oldVoice);
                                log.info("기존 Voice 엔티티 삭제 완료: {}", oldVoice.getId());
                            } catch (Exception e) {
                                log.warn("기존 Voice 삭제 실패 - voiceId: {}", oldVoice.getId(), e);
                            }
                        }
                    }
                }
        );
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
        hall.setVoice(null);

        TransactionSynchronizationManager.registerSynchronization(
            new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    if (oldVoice != null) {
                        try {
                            if (oldVoice.getS3Key() != null) { s3Service.deleteFile(oldVoice.getS3Key());}
                            if (oldVoice.getVoiceId() != null) {elevenLabsClient.deleteVoice(oldVoice.getVoiceId());}
                            voiceRepository.delete(oldVoice);
                        } catch (Exception e) {
                            log.warn("Voice 삭제 실패", e);
                        }
                    }
                }
            }
        );
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

        String getUrl = s3Service.generatePresignedDownloadUrl(voice.getS3Key());

        return VoiceDTO.builder()
                .url(getUrl)
                .build();
    }

}
