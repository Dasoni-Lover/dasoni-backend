package dasoni_backend.domain.voice.service;

import dasoni_backend.domain.user.entity.User;
import dasoni_backend.domain.voice.dto.VoiceDTOs.VoiceDTO;

public interface VoiceService {

    // 추모관 음성파일 업로드
    void uploadVoice(Long hallId, VoiceDTO request, User user);

    // 추모관 음성파일 수정
    void updateVoice(Long hallId, VoiceDTO request, User user);

    VoiceDTO getVoice(Long hallId, User user);
}
