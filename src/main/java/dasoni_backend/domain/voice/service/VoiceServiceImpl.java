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
    private final VoiceServiceTx voiceServiceTx;

    @Override
    @Transactional
    public void uploadVoice(Long hallId, VoiceDTO request, User user){
        Hall hall = hallRepository.findById(hallId)
                .orElseThrow(() -> new EntityNotFoundException("Hall not found"));

        if (!hall.getAdmin().getId().equals(user.getId())) {
            throw new IllegalStateException("권한이 없습니다");
        }

        String s3Key = fileUploadService.extractS3Key(request.getUrl());

        Voice voice = Voice.builder()
                .s3Key(s3Key)
                .updateAt(LocalDateTime.now())
                .voiceId(null)
                .build();

        voiceRepository.save(voice);
        hall.setVoice(voice);
        TransactionSynchronizationManager.registerSynchronization(
            new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    try {
                        generateVoiceIdInternal(voice.getId(), hall.getName());
                    } catch (Exception e) {
                        log.error("voiceId 생성 실패 (upload)", e);
                    }
                }
            }
        );
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
        String s3Key = fileUploadService.extractS3Key(request.getUrl());

        Voice newVoice = Voice.builder()
                .s3Key(s3Key)
                .voiceId(null)
                .updateAt(LocalDateTime.now())
                .build();

        voiceRepository.save(newVoice);

        //  Hall에 새 Voice 연결
        hall.setVoice(newVoice);

        log.info("새 Voice 저장 완료 → voiceId: {}", newVoice.getId());
        log.info("Hall FK 업데이트 완료 → hallId: {}", hall.getId());

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {

                        // 1️⃣ 기존 Voice 정리 (있을 때만)
                        if (oldVoice != null) {
                            try {
                                if (oldVoice.getS3Key() != null) {
                                    s3Service.deleteFile(oldVoice.getS3Key());
                                }
                                if (oldVoice.getVoiceId() != null) {
                                    elevenLabsClient.deleteVoice(oldVoice.getVoiceId());
                                }
                                voiceRepository.delete(oldVoice);
                            } catch (Exception e) {
                                log.warn("기존 Voice 삭제 실패", e);
                            }
                        }

                        // 2️⃣ 새 voiceId는 무조건 생성
                        try {
                            generateVoiceIdInternal(newVoice.getId(), hall.getName());
                        } catch (Exception e) {
                            log.error("voiceId 생성 실패 (update)", e);
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

    private void generateVoiceIdInternal(Long voiceId, String hallName) {

        Voice voice = voiceRepository.findById(voiceId)
                .orElseThrow(() -> new EntityNotFoundException("Voice not found"));

        byte[] audioBytes = s3Service.downloadFile(voice.getS3Key());
        String newVoiceId = elevenLabsClient.createIVCVoice(audioBytes, hallName + "_voice");

        voiceServiceTx.updateVoiceId(voiceId, newVoiceId);
    }
}
