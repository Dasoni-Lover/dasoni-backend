package dasoni_backend.domain.voice.service;

import dasoni_backend.domain.voice.entity.Voice;
import dasoni_backend.domain.voice.repository.VoiceRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class VoiceServiceTxImpl implements VoiceServiceTx {

    private final VoiceRepository voiceRepository;

    @Override
    @Transactional
    public void updateVoiceId(Long voiceId, String newVoiceId) {
        Voice voice = voiceRepository.findById(voiceId)
                .orElseThrow(() -> new EntityNotFoundException("Voice not found"));
        voice.setVoiceId(newVoiceId);
        voice.setUpdateAt(LocalDateTime.now());
    }
}

