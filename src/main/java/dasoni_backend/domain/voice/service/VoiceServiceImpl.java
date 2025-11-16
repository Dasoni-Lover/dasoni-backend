package dasoni_backend.domain.voice.service;

import dasoni_backend.domain.hall.entity.Hall;
import dasoni_backend.domain.hall.repository.HallRepository;
import dasoni_backend.domain.user.entity.User;
import dasoni_backend.domain.voice.dto.VoiceDTOs.VoiceDTO;
import dasoni_backend.domain.voice.entity.Voice;
import dasoni_backend.global.S3.service.FileUploadService;
import dasoni_backend.global.elevenlabs.ElevenLabsClient;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class VoiceServiceImpl implements VoiceService {

    private final FileUploadService fileUploadService;
    private final HallRepository hallRepository;
    private final ElevenLabsClient elevenLabsClient;

    @Override
    @Transactional
    public void uploadVoice(Long hallId, VoiceDTO request, User user){
        Hall hall = hallRepository.findById(hallId)
                .orElseThrow(() -> new EntityNotFoundException("Hall not found"));

        if (!user.equals(hall.getAdmin())) {
            throw new IllegalStateException("권한이 없습니다");
        }

        Voice voice = Voice.builder()
                .url(request.getUrl())
                .updateAt(LocalDateTime.now())
                .build();

        hall.setVoice(voice);
        hallRepository.save(hall);
    }

    @Override
    @Transactional
    public void updateVoice(Long hallId, VoiceDTO request, User user){
        Hall hall = hallRepository.findById(hallId)
                .orElseThrow(() -> new EntityNotFoundException("Hall not found"));

        if (!hall.getAdmin().getId().equals(user.getId())) {
            throw new IllegalStateException("권한이 없습니다");
        }

        // 기존 voice 삭제
        Voice oldVoice = hall.getVoice();
        if (oldVoice != null && oldVoice.getUrl() != null) {
            try {
                String oldS3Key = fileUploadService.extractS3Key(oldVoice.getUrl());
                fileUploadService.deleteFile(oldS3Key);
                log.info("기존 음성 파일 삭제 완료: {}", oldS3Key);
            } catch (Exception e) {
                log.warn("기존 음성 파일 삭제 실패: {}", e.getMessage());
            }
        }

        Voice voice = Voice.builder()
                .url(request.getUrl())
                .updateAt(LocalDateTime.now())
                .build();

        // 업데이트 후 저장
        hall.setVoice(voice);
        hallRepository.save(hall);
    }

    @Override
    @Transactional
    public VoiceDTO getVoice(Long hallId, User user){
        Hall hall = hallRepository.findById(hallId)
                .orElseThrow(() -> new EntityNotFoundException("Hall not found"));

        if (!hall.getAdmin().getId().equals(user.getId())) {
            throw new IllegalStateException("권한이 없습니다");
        }

        Voice voice = hall.getVoice();

        return VoiceDTO.builder()
                .url(voice.getUrl())
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
        if(voice == null || voice.getUrl() == null) {
            throw new IllegalStateException("먼저 음성 파일을 업로드해야 합니다.");
        }

        String key = fileUploadService.extractS3Key(voice.getUrl());
        byte[] audioBytes = fileUploadService.downloadFile(key);

        String name = hall.getName() + "_voice";
        String voiceId = elevenLabsClient.createIVCVoice(audioBytes, name);

        voice.setVoiceId(voiceId);
        voice.setUpdateAt(LocalDateTime.now());
    }
}
