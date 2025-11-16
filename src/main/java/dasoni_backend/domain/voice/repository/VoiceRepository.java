package dasoni_backend.domain.voice.repository;

import dasoni_backend.domain.voice.entity.Voice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VoiceRepository extends JpaRepository<Voice, Long> {
}
