package dasoni_backend.domain.voice.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class VoiceDTOs {

    @Getter
    @Setter
    @NoArgsConstructor
    public static class VoiceDTO {
        private String url;
    }
}
